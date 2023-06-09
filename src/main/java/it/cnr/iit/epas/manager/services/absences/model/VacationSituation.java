/*
 * Copyright (C) 2022  Consiglio Nazionale delle Ricerche
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package it.cnr.iit.epas.manager.services.absences.model;

import com.google.common.collect.Lists;
import it.cnr.iit.epas.models.Contract;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.absences.Absence;
import it.cnr.iit.epas.models.absences.AbsenceType;
import it.cnr.iit.epas.models.absences.TakableAbsenceBehaviour.TakeCountBehaviour;
import it.cnr.iit.epas.models.absences.definitions.DefaultAbsenceType;
import it.cnr.iit.epas.utils.DateUtility;
import java.time.LocalDate;
import java.util.List;
import lombok.ToString;

/**
 * Contiene lo stato ferie annuale di un contratto.
 *
 * @author Alessandro Martelli
 *
 */
public class VacationSituation {

  public Person person;
  public Contract contract;
  public int year;
  public LocalDate date;
  
  public VacationSummary lastYear;
  public VacationSummary currentYear;
  public VacationSummary permissions;
  
  public VacationSummaryCached lastYearCached;
  public VacationSummaryCached currentYearCached;
  public VacationSummaryCached permissionsCached;
   
  /**
   * I giorni rimanenti totali.
   */
  public int sumUsableTotal() {
    int totalRemaining = 0;
    if (lastYearCached.exists) {
      totalRemaining += lastYearCached.usableTotal;
    } 
    if (currentYearCached.exists) {
      totalRemaining += currentYearCached.usableTotal;
    }
    if (permissionsCached != null) {
      totalRemaining += permissionsCached.usableTotal;
    }
    return totalRemaining;
  }
  
  
  /**
   * DTO contenente il resoconto della situazione delle ferie.
   */
  @ToString
  public static class VacationSummary {
    
    public TypeSummary type;
    public int year;
    public LocalDate date;  //data situazione. Tipicamenete oggi. Determina maturate e scadute.
    public Contract contract;
    public AbsencePeriod absencePeriod;
    
    /**
     * Constructor.
     */
    public VacationSummary(Contract contract, AbsencePeriod absencePeriod, 
        int year, LocalDate date, TypeSummary type) {
      this.year = year;
      this.type = type;
      this.date = date;
      this.contract = contract;
      this.absencePeriod = absencePeriod;
    }

    /**
     * Tipologia di resconto: ferie o permessi.
     */
    public static enum TypeSummary {
      VACATION, PERMISSION;
    }
    
    /**
     * Numero di giorni totali di ferie/permessi.
     */
    public long total() {
      return computeTotal(absencePeriod);
    }
    

    /**
     * Nuova implementazione: posPartum anno passato.
     */
    public List<Absence> postPartum() {
      if (absencePeriod == null) {
        return Lists.newArrayList();
      }
      return absencePeriod.reducingAbsences;
    }
    
    public long accrued() {
      return computeAccrued(absencePeriod);
    }
    
    public boolean isContractLowerLimit() {
      return isContractLowerLimit(absencePeriod.from);
    }
    
    /**
     * Verifica la data di partenza del contratto in base a date.
     *
     * @param date la data da verificare
     * @return true se date è il limite inferiore del contratto, false altrimenti.
     */
    private boolean isContractLowerLimit(LocalDate date) {
      if (contract.getBeginDate().isEqual(date)) {
        return true;
      }
      return false;
    }
    
    public LocalDate lowerLimit() {
      return absencePeriod.from;
    }
    
    public boolean isContractUpperLimit() {
      return isContractUpperLimit(lastNaturalSubPeriod(absencePeriod).to);
    }
    
    /**
     * Verifica la data di terminazione del contratto in base a date.
     *
     * @param date la data da verificare
     * @return true se date è il limite superiore del contratto, false altrimenti.
     */
    private boolean isContractUpperLimit(LocalDate date) {
      if (contract.calculatedEnd() != null 
          && contract.calculatedEnd().isEqual(date)) {
        return true;
      }
      return false;
    }


    public LocalDate upperLimit() {
      return lastNaturalSubPeriod(absencePeriod).to;
    }
    
    public long used() {
      return computeUsed(absencePeriod);
    }
    
    /**
     * I giorni usabili. (Usufruibili, per i det solo le maturate).
     */
    public long usable() {
      if (expired()) {
        return 0;
      }
      if (date.isBefore(contract.getBeginDate().plusYears(1))) {
        return accrued() - used();
      } else {
        return total() - used();
      }
    }
    
    /**
     * Se il periodo è scaduto alla data dateToCheck. Se absent considera oggi.
     */
    public boolean expired() {
      if (lastNaturalSubPeriod(absencePeriod).to.isAfter(date)) {
        return false;
      }
      return true;
    }
    
    public long usableTotal() { 
      return total() - used();
    }
    
    private long computeTotal(AbsencePeriod period) {
      return period
          .computePeriodTakableAmount(TakeCountBehaviour.sumAllPeriod, date) / 100;
    }
    
    private long computeAccrued(AbsencePeriod period) {
      return period
          .computePeriodTakableAmount(TakeCountBehaviour.sumUntilPeriod, date) / 100;
    }
    
    private long computeUsed(AbsencePeriod period) {
      return period
          .computePeriodTakenAmount(
              TakeCountBehaviour.sumAllPeriod, lastSubPeriod(period).to) / 100;
    }
    

    /**
     * L'ultimo subPeriodo non prorogato (per la data fine). 
     * Se l'ultimo subPeriod è l'estensione 37 seleziono quello ancora precedente.
     */
    private AbsencePeriod lastNaturalSubPeriod(AbsencePeriod period) {
      AbsencePeriod lastPeriod = period.subPeriods.get(period.subPeriods.size() - 1);
      for (AbsenceType taken : lastPeriod.takenCodes) {
        if (taken.getCode().equals(DefaultAbsenceType.A_37.getCode())) {
          return period.subPeriods.get(period.subPeriods.size() - 2);
        }
      }
      return lastPeriod;
    }
    
    /**
     * L'ultimo subPeriod (per la data fine).
     */
    private AbsencePeriod lastSubPeriod(AbsencePeriod period) {
      return period.subPeriods.get(period.subPeriods.size() - 1);
    }
    
    /**
     * L'ultimo subPeriod che ha contribuito alla maturazione. 
     * (VacationCode che lo ha generato è popolato). 
     */
    private AbsencePeriod lastEffectiveSubPeriod() {
      AbsencePeriod lastEffective = null;
      for (AbsencePeriod period : this.absencePeriod.subPeriods) {
        if (period.vacationCode != null) {
          lastEffective = period;
        }
      }
      return lastEffective;
    }
    
    /**
     * I giorni da inizializzazione.
     */
    public int sourced() {
      int sourced = 0;
      for (AbsencePeriod period : absencePeriod.subPeriods) {
        if (period.initialization != null) {
          sourced += period.initialization.getUnitsInput();
        }
      }
      return sourced;
    }
    
    /**
     * Le assenze utilizzate (post inizializzazione).
     */
    public List<Absence> absencesUsed() {
      List<Absence> absencesUsed = Lists.newArrayList();
      for (AbsencePeriod period : absencePeriod.subPeriods) {
        for (DayInPeriod day : period.daysInPeriod.values()) {
          for (TakenAbsence takenAbsence : day.getTakenAbsences()) {
            if (!takenAbsence.beforeInitialization) {
              absencesUsed.add(takenAbsence.absence);
            }
          }
        }
      }
      return absencesUsed;
    }

    /**
     * I giorni che hanno contribuito alla maturazione. (Quelli appartenenti all'anno solare)
     */
    public long accruedDayTotal() {
      long days = 0;
      for (AbsencePeriod period : absencePeriod.subPeriods) {
        if (period.from.getYear() != this.year) {
          return days;
        }
        days = days + period.periodInterval().dayInInterval();
      }
      return days;
    }

    /**
     * I giorni che hanno contribuito alla maturazione fino a oggi. 
     * (Quelli appartenenti all'anno solare)
     */
    public long accruedDay() {
      long days = 0;
      for (AbsencePeriod period : absencePeriod.subPeriods) {
        if (period.from.getYear() != this.year || period.from.isAfter(this.date)) {
          return days;
        }
        days = days + period.periodInterval().dayInInterval();
      }
      return days;
    }

    /**
     * Verifica se dopo un anno di contratto la data ricade nel periodo.
     *
     * @param period il periodo da considerare per l'assenza
     * @return la data in cui termina il primo anno di contratto se ricade nel periodo.
     */
    public LocalDate contractEndFirstYearInPeriod(AbsencePeriod period) {
      if (DateUtility
          .isDateIntoInterval(contract.getBeginDate().plusYears(1), period.periodInterval())) {
        return contract.getBeginDate().plusYears(1);
      }
      return null;
    }
    
    /**
     * Se il subPeriod è stato escluso dal fixPostPartum.
     */
    public boolean subFixedPostPartum(AbsencePeriod period) {
      return period.vacationAmountBeforeInitializationPatch 
          != period.vacationAmountBeforeFixPostPartum;
    }
    
    /**
     * Se il subPeriod è stato maturato.
     */
    public boolean subAccrued(AbsencePeriod period) {
      return subAmount(period) == subAmountAccrued(period);
    }
    
    /**
     * L'ammontare utilizzabile nel subPeriod.
     */
    public long subAmount(AbsencePeriod period) {
      return period.vacationAmountBeforeInitializationPatch;
    }
    
    /**
     * L'ammontare utilizzabile nel subPeriod maturato. 
     */
    public long subAmountAccrued(AbsencePeriod period) {
      if (date.isBefore(period.from)) {
        return 0;
      }
      return period.vacationAmountBeforeInitializationPatch;
    }
    
    /**
     * L'ammontare utilizzabile nel subPeriod prima del fix post partum.
     */
    public long subAmountBeforeFixedPostPartum(AbsencePeriod period) {
      return period.vacationAmountBeforeFixPostPartum;
    }
    
    /**
     * L'ammontare utilizzabile fino al subPeriod.
     */
    public long subTotalAmount(AbsencePeriod period) {
      long total = 0;
      for (AbsencePeriod subPeriod : this.absencePeriod.subPeriods) {
        total = total + this.subAmount(subPeriod);
        if (subPeriod.equals(period)) {
          break;
        }
      }
      return total;
    }
    
    /**
     * I giorni coperti nell'anno fino al subPeriod.
     */
    public long subDayProgression(AbsencePeriod period) {
      long progress = 0;
      for (AbsencePeriod subPeriod : this.absencePeriod.subPeriods) {
        progress = progress + subPeriod.periodInterval().dayInInterval();
        if (subPeriod.equals(period)) {
          break;
        }
      }
      return progress;
    }
    
    /**
     * I giorni di fix post partum del subPeriod.  
     */
    public long subDayPostPartum(AbsencePeriod period) {
      
      long postPartumToAssign = this.postPartum().size();
      long postPartumAssigned = 0;
      
      //parto dall'ultimo period
      AbsencePeriod lastEffective = this.lastEffectiveSubPeriod();
      while (lastEffective != null) {
        if (postPartumToAssign == 0) {
          return 0;
        }
        long periodPostPartum = 0;
        if (postPartumToAssign <= lastEffective.periodInterval().dayInInterval()) {
          periodPostPartum = postPartumToAssign;
        } else {
          periodPostPartum = lastEffective.periodInterval().dayInInterval();
        }
        postPartumAssigned = postPartumAssigned + periodPostPartum;
        postPartumToAssign = postPartumToAssign - periodPostPartum;
        
        if (period.equals(lastEffective)) {
          return periodPostPartum;
        }
        lastEffective = this.absencePeriod.subPeriods
            .get(this.absencePeriod.subPeriods.indexOf(lastEffective) - 1); 
        //esiste sempre altrimenti sarei uscito alla condizione precedente 
        //dal momento che lastEffective appartiene a subPeriods. 
        
      }
      
      return 0;
    }

    /**
     * Progressione dei giorni post partum fino a quel period.
     */
    public long subDayPostPartumProgression(AbsencePeriod period) {
      long progress = 0;
      //parto dall'ultimo period
      AbsencePeriod lastEffective = this.lastEffectiveSubPeriod();
      while (lastEffective != null) {
        if (subDayPostPartum(lastEffective) == 0) {
          return 0;
        }
        progress = progress + subDayPostPartum(lastEffective);
        if (lastEffective.equals(period)) {
          return progress;
        }
        lastEffective = this.absencePeriod.subPeriods
            .get(this.absencePeriod.subPeriods.indexOf(lastEffective) - 1); 
      }
      return progress;
    }

    public long subDayToFixPostPartum(AbsencePeriod period) {
      return period.periodInterval().dayInInterval() - subDayPostPartum(period);
    }
    
    /**
     * Una label che da il titolo.
     * TODO: spostare su message.
     */
    public String title() {
      if (this.type.equals(TypeSummary.VACATION)) {
        return this.contract.person.fullName() + " - " + "Riepilogo Ferie " + this.year;  
      } else {
        return this.contract.person.fullName() + " - " + "Riepilogo Permessi Legge " + this.year;
      }
    }
  }

}
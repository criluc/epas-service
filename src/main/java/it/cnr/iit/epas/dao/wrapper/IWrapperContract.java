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

package it.cnr.iit.epas.dao.wrapper;

import it.cnr.iit.epas.models.Contract;
import it.cnr.iit.epas.models.ContractMonthRecap;
import it.cnr.iit.epas.models.absences.Absence;
import it.cnr.iit.epas.models.absences.AbsenceType;
import it.cnr.iit.epas.utils.DateInterval;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

/**
 * Interfaccia per l'estensione con Wrapper del Contract.
 */
public interface IWrapperContract extends IWrapperModel<Contract> {

  public IWrapperContract setValue(Contract contract);

  /**
   * True se il contratto è l'ultimo contratto della persona per mese e anno selezionati.
   *
   * @param month mese
   * @param year anno
   * @return esito.
   */
  boolean isLastInMonth(int month, int year);

  /**
   * True se il contratto è a tempo determinato.
   *
   * @return esito.
   */
  boolean isDefined();

  /**
   * L'intervallo effettivo per il contratto. Se endContract (data terminazione del contratto) 
   * è presente sovrascrive contract.endDate.
   *
   * @return l'intervallo.
   */
  DateInterval getContractDateInterval();

  /**
   * L'intervallo ePAS per il contratto. Se è presente una data di inizializzazione generale del
   * contratto sovrascrive contract.beginDate.
   *
   * @return l'intervallo.
   */
  DateInterval getContractDatabaseInterval();

  /**
   * L'intervallo ePAS per il contratto dal punto di vista dei buoni pasto. Se è presente una data 
   * di inizializzazione buoni pasto del contratto sovrascrive contract.beginDate.
   *
   * @return l'intervallo.
   */
  DateInterval getContractDatabaseIntervalForMealTicket();

  /**
   * Il riepilogo mensile attualmente persistito (se esiste) per il mese passato come parametro.
   *
   * @param yearMonth mese
   * @return il riepilogo (absent se non presente)
   */
  Optional<ContractMonthRecap> getContractMonthRecap(YearMonth yearMonth); 
  
  /**
   * Il mese del primo riepilogo esistente per il contratto. absent() se non ci sono i dati per
   * costruire il primo riepilogo.
   *
   * @return il mese (absent se non esiste).
   */
  Optional<YearMonth> getFirstMonthToRecap();

  /**
   * Il mese dell'ultimo riepilogo esistente per il contratto (al momento della chiamata).<br>
   * Il mese attuale se il contratto termina dopo di esso. Altrimenti il mese di fine contratto.
   *
   * @return il mese
   */
  YearMonth getLastMonthToRecap();

  /**
   * Se il contratto è stato inizializzato per la parte residuale nel mese passato come argomento.
   *
   * @param yearMonth mese
   * @return esito
   */
  boolean residualInitInYearMonth(YearMonth yearMonth);

  // ##############################################################################################
  // Strumenti di Diagnosi del contratto.
  // ##############################################################################################

  /**
   * Se per il contratto c'è almeno un riepilogo necessario non persistito.
   *
   * @return esito.
   */
  public boolean monthRecapMissing();

  /**
   * Se un riepilogo per il mese passato come argomento non è persistito.
   *
   * @param yearMonth mese
   * @return esito.
   */
  public boolean monthRecapMissing(YearMonth yearMonth);

  /**
   * La data di inizializzazione è la successiva fra la creazione della persona e l'inizio utilizzo
   * del software della sede della persona (che potrebbe cambiare a causa del trasferimento).
   *
   * @return data
   */
  public LocalDate dateForInitialization();

  /**
   * La data di inizializzazione per i buoni pasto. Non è mandatoria, ma è ignorata se precedente
   * l'inizializzazione del contratto.
   *
   * @return data
   */
  public LocalDate dateForMealInitialization();

  /**
   * Se il contratto è finito prima che la sede della persona fosse installata in ePAS.
   *
   * @return esito
   */
  public boolean noRelevant();

  /**
   * Se il contratto necessita di inizializzazione.
   *
   * @return esito
   */
  public boolean initializationMissing();

  /**
   * Se sono state definite sia la inizializzazione generale che quella buoni pasto e quella 
   * dei buoni pasto è precedente a quella generale.
   *
   * @return esito
   */
  public boolean mealTicketInitBeforeGeneralInit();

  /**
   * Se il contratto ha tutti i riepiloghi necessari per calcolare il riepilogo per l'anno
   * passato come parametro.
   *
   * @param yearToRecap anno
   * @return esito
   */
  public boolean hasMonthRecapForVacationsRecap(int yearToRecap);

  /**
   * Ritorna la lista delle assenze nell'intervallo di tempo relative al tipo ab.
   *
   * @return la lista di assenze effettuate dal titolare del contratto del tipo ab nell'intervallo
   *     temporale inter.
   */
  public List<Absence> getAbsenceDays(DateInterval inter, Contract contract, AbsenceType ab);

}
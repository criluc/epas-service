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

package it.cnr.iit.epas.manager.recaps.personstamping;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import it.cnr.iit.epas.dao.wrapper.IWrapperFactory;
import it.cnr.iit.epas.dao.wrapper.IWrapperPersonDay;
import it.cnr.iit.epas.manager.PersonDayManager;
import it.cnr.iit.epas.manager.cache.StampTypeManager;
import it.cnr.iit.epas.manager.configurations.ConfigurationManager;
import it.cnr.iit.epas.manager.configurations.EpasParam;
import it.cnr.iit.epas.manager.configurations.EpasParam.EpasParamValueType.LocalTimeInterval;
import it.cnr.iit.epas.models.Contract;
import it.cnr.iit.epas.models.PersonDay;
import it.cnr.iit.epas.models.PersonalWorkingTime;
import it.cnr.iit.epas.models.StampModificationType;
import it.cnr.iit.epas.models.StampModificationTypeCode;
import it.cnr.iit.epas.models.Stamping;
import it.cnr.iit.epas.models.Stamping.WayType;
import it.cnr.iit.epas.models.WorkingTimeTypeDay;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Oggetto che modella il giorno di una persona nelle viste - personStamping - stampings -
 * dailyPresence - clocks.
 *
 * @author Alessandro Martelli
 */
@Slf4j
@Data
public class PersonStampingDayRecap {

  private static final String MEALTICKET_NOT_YET = "NOT_YET";
  private static final String MEALTICKET_YES = "YES";
  private static final String MEALTICKET_YES_IF_EXIT_NOW = "YES_IF_EXIT_NOW";
  private static final String MEALTICKET_NO = "NO";

  private static StampModificationType fixedStampModificationType = null;

  private PersonDay personDay;
  public IWrapperPersonDay wrPersonDay;
  public Optional<WorkingTimeTypeDay> wttd;
  public Optional<PersonalWorkingTime> pwttd;
  public LocalTimeInterval lunchInterval;
  public LocalTimeInterval workInterval;
  public Optional<LocalTimeInterval> personalWorkInterval;
  public boolean ignoreDay = false;
  public boolean firstDay = false;
  public List<StampingTemplate> stampingsTemplate = Lists.newArrayList();

  // visualizzazioni particolari da spostare
  public String mealTicket;

  public StampModificationType fixedWorkingTimeCode = null;
  public String exitingNowCode = "";

  public List<String> note = Lists.newArrayList();

  /**
   * Costruisce l'oggetto contenente un giorno lavorativo da visualizzare nel tabellone timbrature.
   *
   * @param personDayManager     injected
   * @param stampTypeManager     injected
   * @param wrapperFactory       injected
   * @param configurationManager injected
   * @param personDay            personDay
   * @param numberOfInOut        numero di colonne del tabellone a livello mensile.
   * @param considerExitingNow   se considerare nel calcolo l'uscita in questo momento
   * @param monthContracts       il riepiloghi del mese
   */
  public PersonStampingDayRecap(PersonDayManager personDayManager,
      StampingTemplateFactory stampingTemplateFactory,
      StampTypeManager stampTypeManager, IWrapperFactory wrapperFactory,
      ConfigurationManager configurationManager, PersonDay personDay,
      int numberOfInOut, boolean considerExitingNow,
      Optional<List<Contract>> monthContracts) {

    this.personDay = personDay;

    if (personDay.isToday()) {
      log.debug("Istanziato PersonStampingDayRecap relativo al giorno corrente.");
    }

    //FIXME è stato rimossa la condizione che comportava il calcolo dei festivi solo sui
    // personDay non persistenti per rattoppare la visualizzazione in quei casi in cui i personDay
    // sono stati persistiti senza calcolare questa informazione.
    // Appena l'informazione persistita diventerà affidabile si può reinserire la condizione.
    this.personDay.setHoliday(personDayManager
        .isHoliday(personDay.getPerson(), personDay.getDate()));

    wrPersonDay = wrapperFactory.create(personDay);
    
    wttd = this.wrPersonDay.getWorkingTimeTypeDay();
    
    lunchInterval = (LocalTimeInterval) configurationManager.configValue(
        personDay.getPerson().getOffice(), EpasParam.LUNCH_INTERVAL, personDay.getDate());
    workInterval = (LocalTimeInterval) configurationManager.configValue(
        personDay.getPerson().getOffice(), EpasParam.WORK_INTERVAL, personDay.getDate());
    
    /*Inizio nuovi campi per la gestione dell'orario personalizzato*/
    pwttd = this.wrPersonDay.getPersonalWorkingTime();
    
    personalWorkInterval = personalWorkingTimeInterval(pwttd);
    /*Fine nuovi campi per la gestione dell'orario personalizzato*/
    
    // 1) computazioni: valid/pair timbrature e uscita in questo momento nel caso di oggi
    if (personDayManager.toComputeExitingNow(personDay) 
        && wrPersonDay.getPersonDayContract().isPresent()
        && wttd.isPresent()) {
      
      // se siamo nel caso di compute e se la persona è attiva effetto il que sera sera.
      personDayManager.queSeraSera(personDay, LocalDateTime.now(),
          wrPersonDay.getPreviousForProgressive(), wttd.get(), 
          wrPersonDay.isFixedTimeAtWork(), lunchInterval, workInterval);
    } else { 
      // altrimenti setto le sole valid stamping
      personDayManager.setValidPairStampings(personDay.getStampings());
    }

    // 2) genero le stamping template (colori e timbrature fittizie)
    stampingsTemplate = getStampingsTemplate(personDay.getStampings(), stampingTemplateFactory, 
        numberOfInOut);
    
    note.addAll(getStampingsNote(this.stampingsTemplate));

    boolean thereAreAllDayAbsences = personDayManager.isAllDayAbsences(personDay);

    if (wrPersonDay.isFixedTimeAtWork() && !personDay.isHoliday() && !thereAreAllDayAbsences) {
      if (fixedStampModificationType == null) {
        fixedStampModificationType =
            stampTypeManager.getStampMofificationType(
                StampModificationTypeCode.FIXED_WORKINGTIME);
      }
      fixedWorkingTimeCode = fixedStampModificationType;
    }

    // is sourceContract (solo se monthContracts presente)
    if (monthContracts.isPresent()) {
      for (Contract contract : monthContracts.get()) {

        // Se il giorno è:
        // Precedente all'inizio del contratto
        // Oppure precedente a un'inizializzazione definita
        // Oppure precedente alla data di inserimento della persona
        // v.iene Ignorato

        if (contract.getBeginDate().isAfter(personDay.getDate())
            || (contract.getSourceDateResidual() != null
            && personDay.getDate().isBefore(contract.getSourceDateResidual()))
            || personDay.getDate().isBefore(personDay.getPerson().getBeginDate())) {
          ignoreDay = true;
        }

        if (contract.getBeginDate().isEqual(personDay.getDate())) {
          firstDay = true;
        }
      }
    }

    computeMealTicket(personDay, thereAreAllDayAbsences);
  }


  /**
   * Imposta il valore della colonna buono pasto nel tabellone timbrature.
   *
   * @param personDay              giorno sul quale impostare il valore relativo al buono
   * @param thereAreAllDayAbsences specifica se sono presenti assenze giornaliere
   */
  private void computeMealTicket(PersonDay personDay, boolean thereAreAllDayAbsences) {

    // ##### Giorno ignorato (fuori contratto)
    //FIXME: verificare se serve isPersistent
    if (ignoreDay) { //  || !personDay.isPersistent()) {
      mealTicket = null;
      return;
    }

    // ##### Giorno festivo

    if (personDay.isHoliday() && personDay.getApprovedOnHoliday() <= 0
        && !personDay.isTicketForcedByAdmin()) {
      mealTicket = null;
      return;
    }

    // ##### Giorni futuri

    if (personDay.isFuture()) {
      if (thereAreAllDayAbsences) {
        mealTicket = MEALTICKET_NO;
      } else {
        mealTicket = null;
      }
      return;
    }

    // ##### Giorni Passati e giorno attuale
    // ##### Available

    if (personDay.isTicketAvailable()) {
      if (personDay.isTicketForcedByAdmin()) {
        // si e forzato
        mealTicket = MEALTICKET_YES;
      } else if (personDay.isToday()) {
        if (thereAreAllDayAbsences) {
          // si non forzato oggi con assenze giornalire FIXME: perchè decido qua no?
          mealTicket = MEALTICKET_NO;
        } else {
          if (personDay.isConsideredExitingNow()) {
            // si non forzato oggi considerando l'uscita in questo momento
            mealTicket = MEALTICKET_YES_IF_EXIT_NOW;
          } else {
            // si non forzato oggi senza considerare l'uscita in questo momento
            mealTicket = MEALTICKET_YES;
          }
        }
      } else {
        // si non forzato giorni passati
        mealTicket = MEALTICKET_YES;
      }
      return;
    }

    // ##### Giorni Passati e giorno attuale
    // ##### Not Available

    if (!personDay.isTicketAvailable()) {
      if (personDay.isTicketForcedByAdmin()) {
        // no forzato
        mealTicket = MEALTICKET_NO;
      } else {
        if (personDay.isPast()) {
          // no non forzato giorni passati
          mealTicket = MEALTICKET_NO;
        } else if (personDay.isToday() || !thereAreAllDayAbsences) {
          // no non forzato oggi senza assenze giornaliere
          mealTicket = MEALTICKET_NOT_YET;
        } else {
          // no non forzato oggi con assenze giornaliere
          mealTicket = MEALTICKET_NO;
        }
      }
    }
  }

  /**
   * N.B. deve essere stata precedentemente chiamata la computeValidStamping per la lista stampings.
   *
   * <br> Crea le timbrature da visualizzare nel tabellone timbrature. 
   * <br> 
   * 1) Riempita di timbrature fittizie nelle celle vuote, fino ad arrivare alla dimensione di 
   * numberOfInOut. 
   * <br> 
   * 2) Con associato il colore e il tipo di bordatura da visualizzare nel tabellone.
   */
  private List<StampingTemplate> getStampingsTemplate(List<Stamping> stampings, 
      StampingTemplateFactory stampingTemplateFactory, int numberOfInOut) {

    final List<Stamping> orderedStampings = ImmutableList
        .copyOf(stampings.stream().sorted().collect(Collectors.toList()));
    
    List<Stamping> stampingsForTemplate = Lists.newArrayList();
    
    boolean isLastIn = false;
    
    for (Stamping s : orderedStampings) {
      //sono dentro e trovo una uscita
      if (isLastIn && s.getWay() == WayType.out) {
        //salvo l'uscita
        stampingsForTemplate.add(s);
        isLastIn = false;
        continue;
      }
      //sono dentro e trovo una entrata
      if (isLastIn && s.getWay() == WayType.in) {
        //creo l'uscita fittizia
        Stamping stamping = new Stamping(null, null);
        stamping.setWay(WayType.out);
        stampingsForTemplate.add(stamping);
        //salvo l'entrata
        stampingsForTemplate.add(s);
        isLastIn = true;
        continue;
      }

      //sono fuori e trovo una entrata
      if (!isLastIn && s.getWay() == WayType.in) {
        //salvo l'entrata
        stampingsForTemplate.add(s);
        isLastIn = true;
        continue;
      }

      //sono fuori e trovo una uscita
      if (!isLastIn && s.getWay() == WayType.out) {
        //creo l'entrata fittizia
        Stamping stamping = new Stamping(null, null);
        stamping.setWay(WayType.in);
        stampingsForTemplate.add(stamping);
        //salvo l'uscita
        stampingsForTemplate.add(s);
        isLastIn = false;
      }
    }
    while (stampingsForTemplate.size() < numberOfInOut * 2) {
      if (isLastIn) {
        //creo l'uscita fittizia
        Stamping stamping = new Stamping(null, null);
        stamping.setWay(WayType.out);
        stampingsForTemplate.add(stamping);
        isLastIn = false;
      } else {
        //creo l'entrata fittizia
        Stamping stamping = new Stamping(null, null);
        stamping.setWay(WayType.in);
        stampingsForTemplate.add(stamping);
        isLastIn = true;
      }
    }

    boolean samePair = false;
    for (Stamping stamping : stampingsForTemplate) {

      //La posizione della timbratura all'interno della sua coppia.
      String position = "none";
      if (stamping.getPairId() != 0 && stamping.isIn()) {
        position = "left";
        samePair = true;
      } else if (stamping.getPairId() != 0 && stamping.isOut()) {
        position = "right";
        samePair = false;
      } else if (samePair) {
        position = "center";
      }

      StampingTemplate stampingTemplate = stampingTemplateFactory.create(stamping, position);

      stampingsTemplate.add(stampingTemplate);
    }
    return stampingsTemplate;
  }

  /**
   * La lista delle note in stampingsTemplate.
   *
   * @param stampingsTemplate le timbrature del giorno.
   * @return la lista di note
   */
  private List<String> getStampingsNote(List<StampingTemplate> stampingsTemplate) {
    List<String> note = Lists.newArrayList();
    for (StampingTemplate stampingTemplate : stampingsTemplate) {
      if (stampingTemplate.getStamping().getNote() != null 
          && !stampingTemplate.getStamping().getNote().equals("")) {
        note.add(stampingTemplate.getHour() + ": " + stampingTemplate.getStamping().getNote());
      }
    }
    return note;
  }

  /**
   * Metodo privato per la restituzione dell'intervallo orario.
   *
   * @param pwt il personal working time del dipendente.
   * @return l'intervallo orario di lavoro.
   */
  private Optional<LocalTimeInterval> personalWorkingTimeInterval(
      Optional<PersonalWorkingTime> pwt) {
    if (!pwt.isPresent()) {
      return Optional.empty();
    }
    LocalTimeInterval interval = 
        new LocalTimeInterval(pwt.get().timeSlot.beginSlot, pwt.get().timeSlot.endSlot);
    if (interval.to.isBefore(interval.from)) {
      return Optional.empty();
    } else {
      return Optional.ofNullable(interval);
    }    
  }
}

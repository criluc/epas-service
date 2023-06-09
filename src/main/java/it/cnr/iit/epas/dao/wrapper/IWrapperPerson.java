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

import it.cnr.iit.epas.models.CertificatedData;
import it.cnr.iit.epas.models.Competence;
import it.cnr.iit.epas.models.CompetenceCode;
import it.cnr.iit.epas.models.Contract;
import it.cnr.iit.epas.models.ContractStampProfile;
import it.cnr.iit.epas.models.ContractWorkingTimeType;
import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.VacationPeriod;
import it.cnr.iit.epas.models.WorkingTimeType;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

/**
 * Oggetto persone con molte funzionalità aggiuntive.
 *
 * @author Marco Andreini
 */
public interface IWrapperPerson extends IWrapperModel<Person> {

  public IWrapperPerson setValue(Person person);
  
  /**
   * Se la persona ha contratto attivo nella data.
   */
  boolean isActiveInDay(LocalDate date);

  /**
   * Se la persona ha contratto attivo nel mese.
   */
  boolean isActiveInMonth(YearMonth yearMonth);

  /**
   * Il contratto attuale. Istanzia una variabile Lazy.
   */
  Optional<Contract> getCurrentContract();
  
  /**
   * Il contratto precedente, se esiste.
   */
  Optional<Contract> getPreviousContract();


  /**
   * Il piano ferie attuale. Istanzia una variabile Lazy.
   */
  Optional<VacationPeriod> getCurrentVacationPeriod();


  /**
   * Il tipo orario attuale. Istanzia una variabile Lazy.
   */
  Optional<WorkingTimeType> getCurrentWorkingTimeType();

  /**
   * Il periodo del tipo orario attuale. Istanzia una variabile Lazy.
   */
  Optional<ContractWorkingTimeType> getCurrentContractWorkingTimeType();

  /**
   * Il tipo timbratura attuale. Istanzia una variabile Lazy.
   */
  Optional<ContractStampProfile> getCurrentContractStampProfile();

  /**
   * I contratti della persona ordinati per date crescenti.
   */
  List<Contract> orderedContracts();
  
  /**
   * I contratti della persona nell'anno ordinati per date crescenti.
   */
  List<Contract> orderedYearContracts(int year);

  /**
   * I contratti della persona nel mese ordinati per date crescenti.
   */
  List<Contract> orderedMonthContracts(int year, int month);

  /**
   * L'ultimo contratto attivo della persona nel mese.
   */
  Optional<Contract> getLastContractInMonth(int year, int month);

  /**
   * Il primo contratto attivo della persona nel mese.
   */
  Optional<Contract> getFirstContractInMonth(int year, int month);

  /**
   * L'ultimo mese con contratto attivo.
   */
  YearMonth getLastActiveMonth();

  /**
   * True se la persona è passata da determinato a indeterminato durante l'anno.
   */
  boolean hasPassToIndefiniteInYear(int year);

  /**
   * L'esito dell'invio attestati per la persona (null se non è ancora stato effettuato).
   */
  CertificatedData getCertificatedData(int year, int month);

  /**
   * Getter per la competenza della persona tramite CompetenceCode, year e month.
   */
  Competence competence(final CompetenceCode code, final int year, final int month);

  /**
   * Il residuo positivo del mese fatto dalla person.
   */
  Integer getPositiveResidualInMonth(int year, int month);

  /**
   * Diagnostiche sui dati della persona.
   */
  boolean currentContractInitializationMissing();

  boolean currentContractMonthRecapMissing();
  
  /**
   * Diagnostiche sullo stato di sincronizzazione della persona.
   * Ha perseoId null oppure uno dei suoi contratti attivi o futuri ha perseoId null.
   */
  boolean isProperSynchronized();
  
  /**
   * Il contratto della persona con quel perseoId.
   *
   * @param perseoId perseoId
   * @return contratto
   */
  Contract perseoContract(String perseoId);
  
  /**
   * Se una persona è IV-VIII livello.
   *
   * @return true se la persona è un tecnico (liv. IV - VIII), false altrimenti
   */
  boolean isTechnician();

  /**
   * L'ultimo invio attestati effettuato tramite ePAS.
   */
  Optional<YearMonth> lastUpload();

  public int getNumberOfMealTicketsPreviousMonth(YearMonth yearMonth);
}
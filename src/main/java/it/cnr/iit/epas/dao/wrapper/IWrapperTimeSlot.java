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
import it.cnr.iit.epas.models.ContractMandatoryTimeSlot;
import it.cnr.iit.epas.models.Office;
import it.cnr.iit.epas.models.TimeSlot;
import java.util.List;

/**
 * Oggetto TimeSlot con funzionalità aggiuntive.
 */
public interface IWrapperTimeSlot extends IWrapperModel<TimeSlot> {

  public IWrapperTimeSlot setValue(TimeSlot ts);

  /**
   * I contratti attivi che attualmente hanno impostato il TimeSlot.
   */
  List<Contract> getAssociatedActiveContract(Office office);

  /**
   * Ritorna i periodi con questo tipo orario appartenti a contratti attualmente attivi.
   */
  List<ContractMandatoryTimeSlot> getAssociatedPeriodInActiveContract(Office office);

  /**
   * Tutti i contratti associati alla fascia di orario lavorativo. 
   */
  List<Contract> getAssociatedContract();

}
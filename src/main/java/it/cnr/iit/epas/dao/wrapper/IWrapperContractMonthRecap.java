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

import it.cnr.iit.epas.models.ContractMonthRecap;
import java.util.Optional;

/**
 * Contratto con alcune funzionalità aggiuntive.
 *
 * @author Alessandro Martelli
 */
public interface IWrapperContractMonthRecap extends IWrapperModel<ContractMonthRecap> {

  public IWrapperContractMonthRecap setValue(ContractMonthRecap cmr);

  public IWrapperContract getContract();

  public boolean residualInitInYearMonth(int year, int month);

  public Optional<ContractMonthRecap> getPreviousRecap();

  public Optional<ContractMonthRecap> getPreviousRecapInYear();

  /**
   * Se visualizzare il prospetto sul monte ore anno precedente.
   */
  public boolean hasResidualLastYear();

  /**
   * Il valore iniziale del monte ore anno precedente.
   */
  public int getResidualLastYearInit();

}
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

package it.cnr.iit.epas.models.enumerate;

/**
 * Enumerato per la gestione dei riposi compensativi I-III.
 *
 * @author Dario Tagliaferri
 */
public enum CapacityCompensatoryRestOneThree {

  onDayResidual("residuo del giorno"),
  onEndOfMonthResidual("residuo a fine mese"),
  onEndPastMonthResidual("residuo a fine mese precedente"),
  onEndPastQuarterResidual("residuo a trimestre precedente");

  public String description;

  private CapacityCompensatoryRestOneThree(String description) {
    this.description = description;
  }
}

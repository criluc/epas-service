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
 * Rappresenta le tipologie di problemi sulle timbrature.
 */
public enum Troubles {

  UNCOUPLED_FIXED("timbratura disaccoppiata persona fixed"),
  NO_ABS_NO_STAMP("no assenze giornaliere e no timbrature"),
  UNCOUPLED_WORKING("timbratura disaccoppiata giorno feriale"),
  UNCOUPLED_HOLIDAY("timbratura disaccoppiata giorno festivo"),
  NOT_ENOUGH_WORKTIME("tempo a lavoro insufficiente");

  public String description;

  Troubles(String description) {
    this.description = description;
  }

}

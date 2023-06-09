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

package it.cnr.iit.epas.models.exports;

import it.cnr.iit.epas.models.Person;
import it.cnr.iit.epas.models.PersonReperibilityType;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.joda.time.LocalDate;

/**
 * Classe di supporto per l'importazione delle informazioni relative ai giorni di assenza delle
 * persone reperibili.
 *
 * @author Arianna Del Soldato
 */
@RequiredArgsConstructor
@AllArgsConstructor
public class AbsenceReperibilityPeriod {

  public final Person person;
  public final LocalDate start;
  public LocalDate end;
  public final PersonReperibilityType reperibilityType;

}

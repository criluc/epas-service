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

package it.cnr.iit.epas.dto.v4;

import java.time.LocalDate;
import lombok.Data;

/**
 * DTO per l'esportazione via REST delle informazioni 
 * principali delle ferie.
 *
 * @since versione 4 dell'API REST
 * @author Andrea Generosi
 *
 */
@Data
public class VacationSituationDto {

  private Long personId;
  private int year;
  private LocalDate date;
  private ContractShowDto contract;

  private VacationSummaryTerseDto lastYear;
  private VacationSummaryTerseDto currentYear;
  private VacationSummaryTerseDto permissions;

}
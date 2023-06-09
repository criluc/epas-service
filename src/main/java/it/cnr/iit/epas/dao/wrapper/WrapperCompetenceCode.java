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

import com.google.common.collect.Lists;
import it.cnr.iit.epas.dao.CompetenceDao;
import it.cnr.iit.epas.dao.OfficeDao;
import it.cnr.iit.epas.models.Competence;
import it.cnr.iit.epas.models.CompetenceCode;
import it.cnr.iit.epas.models.Office;
import java.util.List;
import javax.inject.Inject;
import org.springframework.stereotype.Component;

/**
 * Oggetto CompetenceCode con funzionalità aggiuntive.
 */
@Component
public class WrapperCompetenceCode implements IWrapperCompetenceCode {

  private CompetenceCode value;
  private final CompetenceDao competenceDao;
  private final OfficeDao officeDao;

  @Inject
  WrapperCompetenceCode(
       OfficeDao officeDao, CompetenceDao competenceDao) {
    this.competenceDao = competenceDao;
    this.officeDao = officeDao;
  }

  public IWrapperCompetenceCode setValue(CompetenceCode cc) {
    this.value = cc;
    return this;
  }

  @Override
  public CompetenceCode getValue() {
    return value;
  }

  /**
   * Il totale delle competenze per quel mese.
   *
   * @return il totale per quel mese e quell'anno di ore/giorni relativi a quel codice competenza.
   */
  public int totalFromCompetenceCode(int month, int year, Long officeId) {

    Office office = officeDao.getOfficeById(officeId);

    int totale = 0;
    List<String> competenceCodeList = Lists.newArrayList();
    competenceCodeList.add(this.value.code);

    List<Competence> compList = competenceDao.getCompetencesInOffice(year, month,
            competenceCodeList, office, false);

    for (Competence comp : compList) {
      totale = totale + comp.valueApproved;
    }
    return totale;
  }

}

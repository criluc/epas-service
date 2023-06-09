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

import it.cnr.iit.epas.dao.wrapper.IWrapperFactory;
import it.cnr.iit.epas.manager.PersonDayManager;
import it.cnr.iit.epas.manager.cache.StampTypeManager;
import it.cnr.iit.epas.manager.configurations.ConfigurationManager;
import it.cnr.iit.epas.models.Contract;
import it.cnr.iit.epas.models.PersonDay;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import org.springframework.stereotype.Component;

/**
 * Factory per PersonStampingDayRecap.
 */
@Component
public class PersonStampingDayRecapFactory {

  public final IWrapperFactory wrapperFactory;
  public final StampTypeManager stampTypeManager;
  private final PersonDayManager personDayManager;
  private final StampingTemplateFactory stampingTemplateFactory;
  private final ConfigurationManager configurationManager;

  /**
   * Costruttore per l'injection.
   */
  @Inject
  PersonStampingDayRecapFactory(PersonDayManager personDayManager,
      StampingTemplateFactory stampingTemplateFactory,
      StampTypeManager stampTypeManager, IWrapperFactory wrapperFactory,
      ConfigurationManager configurationManager) {
    this.personDayManager = personDayManager;
    this.stampingTemplateFactory = stampingTemplateFactory;
    this.stampTypeManager = stampTypeManager;
    this.wrapperFactory = wrapperFactory;
    this.configurationManager = configurationManager;
  }

  /**
   * Costruisce l'oggetto che rappresenta un giorno nel tabellone timbrature.
   *
   * @param personDay          personDay
   * @param numberOfInOut      numero di colonne del tabellone a livello mensile.
   * @param considerExitingNow se considerare nel calcolo l'uscita in questo momento
   * @param monthContracts     riepiloghi mensili (servono a capire se il giorno è da considere).
   * @return personStampingDayRecap
   */
  public PersonStampingDayRecap create(PersonDay personDay, int numberOfInOut,
      boolean considerExitingNow, Optional<List<Contract>> monthContracts) {

    return new PersonStampingDayRecap(personDayManager, stampingTemplateFactory, stampTypeManager,
        wrapperFactory, configurationManager,
        personDay, numberOfInOut, considerExitingNow, monthContracts);
  }

}

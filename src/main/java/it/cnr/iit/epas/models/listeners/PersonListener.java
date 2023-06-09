/*
 * Copyright (C) 2023  Consiglio Nazionale delle Ricerche
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

package it.cnr.iit.epas.models.listeners;

import it.cnr.iit.epas.models.Person;
import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Listener per alcune azioni effettuate sul oggetto del modello Person.
 *
 * @author Cristian Lucchesi
 *
 */
@Transactional
@Slf4j
@Component
public class PersonListener {

  private final Provider<EntityManager> emp;

  @Inject
  PersonListener(Provider<EntityManager> emp) {
    this.emp = emp;
  }

  @PreUpdate
  private void onUpdate(Person person) {
    person.setUpdatedAt(LocalDateTime.now());
  }

  @PrePersist
  private void onCreation(Person person) {
    // TODO meglio rendere non necessario questo barbatrucco...
    if (person.getBeginDate() == null) {
      person.setBeginDate(LocalDate.now().minusYears(1).withMonth(12).withDayOfMonth(31));
    }
    person.setUpdatedAt(LocalDateTime.now());
  }

  @PreRemove
  void preRemove(Person person) {
    log.debug("Invocato personListener::preRemove. Person = {}, groups = {}",
        person, person.getGroups());
    person.getAffiliations().stream().forEach(affiliation -> {
      emp.get().remove(affiliation);
      log.info("Rimossa associazione {} a gruppo {}", 
          person.getFullname(), affiliation.getGroup().getName());
    });
  }
}
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

package it.cnr.iit.epas.models;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.cnr.iit.epas.models.base.BaseEntity;
import it.cnr.iit.epas.models.enumerate.AccountRole;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

/**
 * Un utente di ePAS.
 */
@Getter
@Setter
@Entity
@Audited
@Table(name = "users", uniqueConstraints = {@UniqueConstraint(columnNames = {"username"})})
public class User extends BaseEntity {

  private static final long serialVersionUID = -6039180733038072891L;

  //@Unique
  @NotNull
  @Column(nullable = false)
  private String username;

  @Deprecated
  @Size(min = 5)
  private String password;
  
  private String passwordSha512;
  
  /**
   * Corrisponde ad un identificato univoco nella propria 
   * organizzazione per questo utente.
   * Se l'utente è una persona deve corrispondere con il campo
   * eppn.
   */
  private String subjectId;

  @NotAudited
  @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
  private Person person;

  @NotAudited
  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
  private List<BadgeReader> badgeReaders = Lists.newArrayList();

  @ElementCollection
  @Enumerated(EnumType.STRING)
  private Set<AccountRole> roles = Sets.newHashSet();

  
  @OneToMany(mappedBy = "user", cascade = {CascadeType.REMOVE})
  private List<UsersRolesOffices> usersRolesOffices = new ArrayList<UsersRolesOffices>();

  @Column(name = "expire_recovery_token")
  private LocalDate expireRecoveryToken;

  @Column(name = "recovery_token")
  private String recoveryToken;

  @Column(name = "disabled")
  private boolean disabled;

  @Column(name = "expire_date")
  private LocalDate expireDate;

  @Nullable
  @ManyToOne
  @JoinColumn(name = "office_owner_id")
  public Office owner;

  public String keycloakId;

  /**
   * Ritorna il badgeReader associato all'utente se ne ha almeno uno associato.
   *
   * @return il badgeReader associato all'utente se ne ha almeno uno associato.
   */
  @Transient
  public BadgeReader getBadgeReader() {
    if (badgeReaders.size() > 0) {
      return badgeReaders.get(0);
    }
    return null;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("id", this.getId())
        .add("user", this.username)
        .toString();
  }

  /**
   * True se l'user ha un ruolo di sistema.
   */
  public boolean isSystemUser() {
    return !roles.isEmpty();
  }

  /**
   * L'office fornito ha un legame con questo uro.
   *
   * @return true se ce l'ha, false altrimenti.
   */
  public boolean hasRelationWith(Office office) {
    return owner == office || (person != null && person.getOffice() == office);
  }

  /**
   * True se l'utente ha almeno uno dei ruoli passati tra i parametri,
   * false altrimenti.
   *
   * @param args Stringhe corrispondenti ai ruoli da verificare.
   * @return true se contiene almeno uno dei ruoli specificati.
   */
  public boolean hasRoles(String... args) {
    return usersRolesOffices.stream()
        .anyMatch(uro -> Arrays.asList(args).contains(uro.role.getName()));
  }
}

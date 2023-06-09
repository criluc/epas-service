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

import com.google.common.collect.Lists;
import it.cnr.iit.epas.models.base.BaseEntity;
import java.util.List;
import javax.annotation.Nullable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import org.checkerframework.common.aliasing.qual.Unique;


/**
 * Una zona di timbratura. Utilizzate per calcoli particolari tra zone collegate.
 */
@Entity
@Table(name = "zones")
public class Zone extends BaseEntity {

  private static final long serialVersionUID = 2466096445310199806L;

  @Unique
  @NotNull
  public String name;
  
  public String description;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "badge_reader_id")
  @Nullable
  public BadgeReader badgeReader;
  
  @OneToMany(mappedBy = "zoneBase")
  public List<ZoneToZones> zoneLinkedAsMaster = Lists.newArrayList();
  
  @OneToMany(mappedBy = "zoneLinked")
  public List<ZoneToZones> zoneLinkedAsSlave = Lists.newArrayList();
  
  /* Utilizzata nelle select html per mostrare questa zona.
   * @see models.base.BaseModel#getLabel()
   */
  @Override
  public String getLabel() {
    return name;
  }
  
  @Override
  public String toString() {
    return String.format(
        "Zone[%d] - %s", getId(), this.name);
  }
}
/*
 * Copyright (c) 2010 The original author(s)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.peholmst.springsecuritydemo.domain;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * Domain class that represents a category. Categories can be nested.
 * 
 * @author Petter Holmström
 */
@Entity
public class Category extends AbstractEntity {

  private static final long serialVersionUID = -3513404373476593420L;

  private String name;

  private String description;

  @ManyToOne
  private Category parent;

  public Category() {
    this("", "");
  }

  public Category(String name, String description) {
    this(name, description, null);
  }

  public Category(String name, String description, Category parent) {
    this.name = name;
    this.description = description;
    this.parent = parent;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Category other = (Category) obj;
    if (description == null) {
      if (other.description != null) {
        return false;
      }
    } else if (!description.equals(other.description)) {
      return false;
    }
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    if (parent == null) {
      if (other.parent != null) {
        return false;
      }
    } else if (!parent.equals(other.parent)) {
      return false;
    }
    return true;
  }

  public String getDescription() {
    return description;
  }

  public String getName() {
    return name;
  }

  public Category getParent() {
    return parent;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + (description == null ? 0 : description.hashCode());
    result = prime * result + (name == null ? 0 : name.hashCode());
    result = prime * result + (parent == null ? 0 : parent.hashCode());
    return result;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setParent(Category parent) {
    this.parent = parent;
  }
}

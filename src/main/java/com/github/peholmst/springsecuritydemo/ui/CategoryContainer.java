/*
 * Copyright 2010 Petter Holmström
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
package com.github.peholmst.springsecuritydemo.ui;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.github.peholmst.springsecuritydemo.domain.Category;
import com.github.peholmst.springsecuritydemo.services.CategoryService;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;

/**
 * This class implements a hierarchical container of {@link Category} instances.
 * It has been designed to be used explicitly with {@link com.vaadin.ui.Tree}.
 * It will not work with combo boxes or tables as most of the methods defined in
 * the {@link Container} interface are not implemented.
 * <p>
 * The categories are fetched from a {@link CategoryService} and are stored in a
 * local cache. The container can be refreshed by calling {@link #refresh()}.
 * The container is read-only, i.e. changes have to be made to the category
 * service directly.
 * <p>
 * The category id ({@link Category#getId()}) is used as the Item ID.
 * 
 * @author Petter Holmström
 */
public class CategoryContainer implements Container.Hierarchical,
    Container.ItemSetChangeNotifier {

  /**
   * Event fired when the {@link CategoryContainer} has been refreshed with
   * fresh data from the category service.
   * 
   * @author Petter Holmström
   */
  public class ContainerRefreshedEvent implements ItemSetChangeEvent {

    private static final long serialVersionUID = 2098472936710486939L;

    @Override
    public Container getContainer() {
      return CategoryContainer.this;
    }

  }

  /**
   * This class represents a node in the category tree. It is used to store the
   * {@link Category} instances locally inside the container.
   * 
   * @author Petter Holmström
   */
  protected class Node {
    private Node parent;
    private List<Node> children;
    private List<Long> childrenIds;
    private long lastUpdated;
    private Category category;

    /**
     * Creates a new <code>Node</code>.
     * 
     * @param parent
     *          the parent node, or <code>null</code> if the node is a root.
     * @param category
     *          the category that this node represents (must not be
     *          <code>null</code>).
     */
    public Node(Node parent, Category category) {
      assert category != null : "category must not be null";
      this.parent = parent;
      this.category = category;
      this.lastUpdated = System.currentTimeMillis();
    }

    /**
     * Gets the category instance that this node represents.
     * 
     * @return the category, never <code>null</code>.
     */
    public Category getCategory() {
      return category;
    }

    /**
     * Gets the child nodes of this category. The first time this method is
     * called, the children will be loaded from the category service.
     * 
     * @return an unmodifiable list of child nodes.
     */
    public List<Node> getChildren() {
      if (children == null) {
        children = new LinkedList<Node>();
        childrenIds = new LinkedList<Long>();
        List<Category> childCategories = getCategoryService().getChildren(
            category);
        for (Category c : childCategories) {
          children.add(getNode(c));
          childrenIds.add(c.getId());
        }
        children = Collections.unmodifiableList(children);
        childrenIds = Collections.unmodifiableList(childrenIds);
      }
      return children;
    }

    /**
     * Gets the IDs of all the child categories.
     * 
     * @return an unmodifiable list of child IDs.
     */
    public List<Long> getChildrenIds() {
      if (childrenIds == null) {
        getChildren();
      }
      return childrenIds;
    }

    /**
     * Gets the timestamp when this node was created (i.e. fetched from the
     * category service).
     * 
     * @return the timestamp in milliseconds (as returned by
     *         {@link System#currentTimeMillis()}).
     */
    public long getLastUpdated() {
      return lastUpdated;
    }

    /**
     * Gets the parent node of this node.
     * 
     * @return the parent node, or <code>null</code> if the node is a root node.
     */
    public Node getParent() {
      return parent;
    }
  }

  private static final long serialVersionUID = 1197578205205304787L;

  private final CategoryService categoryService;

  private final Map<String, Class<?>> propertyIds;

  private Map<Long, Node> idToNodeMap = Collections.emptyMap();

  private List<Long> rootIds = Collections.emptyList();

  private LinkedList<ItemSetChangeListener> listeners;

  /**
   * Creates a new <code>CategoryContainer</code>.
   * 
   * @param categoryService
   *          the category service to use (must not be <code>null</code>).
   */
  public CategoryContainer(CategoryService categoryService) {
    assert categoryService != null : "categoryService must not be null";
    this.categoryService = categoryService;

    propertyIds = new HashMap<String, Class<?>>();
    try {
      BeanInfo bi = Introspector.getBeanInfo(Category.class);
      for (PropertyDescriptor pd : bi.getPropertyDescriptors()) {
        if (!pd.getName().equals("class")) {
          propertyIds.put(pd.getName(), pd.getPropertyType());
        }
      }
    } catch (Exception e) {
      // Should never happen
      throw new RuntimeException(
          "Could not read properties from Category class", e);
    }
  }

  /**
   * <strong>This method is not implemented!</strong>
   * <p>
   * {@inheritDoc}
   */
  @Override
  public boolean addContainerProperty(Object propertyId, Class<?> type,
      Object defaultValue) throws UnsupportedOperationException {
    throw new UnsupportedOperationException(); // Not implemented
  }

  /**
   * <strong>This method is not implemented!</strong>
   * <p>
   * {@inheritDoc}
   */
  @Override
  public Object addItem() throws UnsupportedOperationException {
    throw new UnsupportedOperationException(); // Not implemented
  }

  /**
   * <strong>This method is not implemented!</strong>
   * <p>
   * {@inheritDoc}
   */
  @Override
  public Item addItem(Object itemId) throws UnsupportedOperationException {
    throw new UnsupportedOperationException(); // Not implemented
  }

  @Override
  public void addListener(ItemSetChangeListener listener) {
    if (listener == null) {
      return;
    }
    if (listeners == null) {
      listeners = new LinkedList<ItemSetChangeListener>();
    }
    listeners.add(listener);
  }

  @Override
  public boolean areChildrenAllowed(Object itemId) {
    return hasChildren(itemId);
  }

  @Override
  public boolean containsId(Object itemId) {
    return getNode((Long) itemId) != null;
  }

  /**
   * Sends <code>event</code> to all registered {@link ItemSetChangeListener} s.
   * If the event is null, nothing happens.
   * 
   * @param event
   *          the event to fire.
   */
  @SuppressWarnings("unchecked")
  protected void fireItemSetChange(ItemSetChangeEvent event) {
    if (event == null || listeners == null) {
      return;
    }
    /*
     * Iterate over a cloned list instead of the list itself to avoid strange
     * behavior if any of the listeners add additional listeners or remove
     * existing ones.
     */
    LinkedList<ItemSetChangeListener> clonedList = (LinkedList<ItemSetChangeListener>) listeners
        .clone();
    for (ItemSetChangeListener listener : clonedList) {
      listener.containerItemSetChange(event);
    }
  }

  /**
   * Gets the category service that is used to fetch categories.
   * 
   * @return the category service (never <code>null</code>).
   */
  public CategoryService getCategoryService() {
    return categoryService;
  }

  @Override
  public Collection<?> getChildren(Object itemId) {
    Node node = getNode((Long) itemId);
    return node == null ? null : node.getChildrenIds();
  }

  @Override
  public Property getContainerProperty(Object itemId, Object propertyId) {
    Item item = getItem(itemId);
    return item == null ? null : item.getItemProperty(propertyId);
  }

  @Override
  public Collection<?> getContainerPropertyIds() {
    return Collections.unmodifiableCollection(propertyIds.keySet());
  }

  @Override
  public BeanItem<Category> getItem(Object itemId) {
    Node node = getNode((Long) itemId);
    return node == null ? null : new BeanItem<Category>(node.getCategory());
  }

  /**
   * <strong>This method is not implemented!</strong>
   * <p>
   * {@inheritDoc}
   */
  @Override
  public Collection<?> getItemIds() {
    throw new UnsupportedOperationException(); // Not implemented
  }

  /**
   * Gets the node that represents <code>category</code>. If no node is found,
   * one is created. This method does not access the category service.
   * 
   * @param category
   *          the category whose node should be fetched (must not be
   *          <code>null</code>).
   * @return the node (never <code>null</code>).
   */
  protected Node getNode(Category category) {
    assert category != null : "category must not be null";
    Node node = idToNodeMap.get(category.getId());
    if (node == null) {
      Node parentNode = category.getParent() == null ? null : getNode(category
          .getParent().getId());
      node = new Node(parentNode, category);
      idToNodeMap.put(category.getId(), node);
    }
    return node;

  }

  /**
   * Gets the node identified by <code>id</code>. The first time this method is
   * called, the category is fetched from the category service.
   * 
   * @param id
   *          the ID of the node to fetch (must not be <code>null</code>).
   * @return the node, or <code>null</code> if it could not be found.
   */
  protected Node getNode(Long id) {
    assert id != null : "id must not be null";
    Node node = idToNodeMap.get(id);
    if (node == null) {
      Category c = getCategoryService().getCategoryById(id);
      if (c == null) {
        return null;
      }
      Node parentNode = c.getParent() == null ? null : getNode(c.getParent()
          .getId());
      node = new Node(parentNode, c);
      idToNodeMap.put(id, node);
    }
    return node;
  }

  @Override
  public Object getParent(Object itemId) {
    Node node = getNode((Long) itemId);
    return node == null || node.getParent() == null ? null : node.getParent()
        .getCategory().getId();
  }

  @Override
  public Class<?> getType(Object propertyId) {
    return propertyIds.get(propertyId);
  }

  @Override
  public boolean hasChildren(Object itemId) {
    Node node = getNode((Long) itemId);
    return node == null ? false : !node.getChildren().isEmpty();
  }

  @Override
  public boolean isRoot(Object itemId) {
    return rootItemIds().contains(itemId);
  }

  /**
   * Refreshes the container and fires a {@link ContainerRefreshedEvent}.
   */
  public void refresh() {
    rootIds = new LinkedList<Long>();
    idToNodeMap = new HashMap<Long, Node>();
    for (Category c : getCategoryService().getRootCategories()) {
      rootIds.add(c.getId());
    }
    rootIds = Collections.unmodifiableList(rootIds);
    fireItemSetChange(new ContainerRefreshedEvent());
  }

  /**
   * <strong>This method is not implemented!</strong>
   * <p>
   * {@inheritDoc}
   */
  @Override
  public boolean removeAllItems() throws UnsupportedOperationException {
    throw new UnsupportedOperationException(); // Not implemented
  }

  /**
   * <strong>This method is not implemented!</strong>
   * <p>
   * {@inheritDoc}
   */
  @Override
  public boolean removeContainerProperty(Object propertyId)
      throws UnsupportedOperationException {
    throw new UnsupportedOperationException(); // Not implemented
  }

  /**
   * <strong>This method is not implemented!</strong>
   * <p>
   * {@inheritDoc}
   */
  @Override
  public boolean removeItem(Object itemId) throws UnsupportedOperationException {
    throw new UnsupportedOperationException(); // Not implemented
  }

  @Override
  public void removeListener(ItemSetChangeListener listener) {
    if (listener == null || listeners == null) {
      return;
    }
    listeners.remove(listener);
    if (listeners.isEmpty()) {
      listeners = null;
    }
  }

  @Override
  public Collection<Long> rootItemIds() {
    return rootIds;
  };

  /**
   * <strong>This method is not implemented!</strong>
   * <p>
   * {@inheritDoc}
   */
  @Override
  public boolean setChildrenAllowed(Object itemId, boolean areChildrenAllowed)
      throws UnsupportedOperationException {
    return false; // Not implemented
  }

  /**
   * <strong>This method is not implemented!</strong>
   * <p>
   * {@inheritDoc}
   */
  @Override
  public boolean setParent(Object itemId, Object newParentId)
      throws UnsupportedOperationException {
    throw new UnsupportedOperationException(); // Not implemented
  }

  /**
   * <strong>This method is not implemented!</strong>
   * <p>
   * {@inheritDoc}
   */
  @Override
  public int size() {
    throw new UnsupportedOperationException(); // Not implemented
  }

}

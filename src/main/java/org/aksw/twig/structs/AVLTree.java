package org.aksw.twig.structs;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Implementation of an AVL tree. An AVL tree is roughly the same as a normal binary tree but is
 * guaranteed to run search, insert and delete operations in {@code O(log n)} with {@code n} being
 * the size of the tree.<br>
 * Note that in this implementation only {@link #contains(Comparable)}, {@link #remove(Comparable)}
 * are guaranteed to run in {@code O(log n)} whereas {@link #contains(Object)},
 * {@link #remove(Object)} will run in {@code O(n)}.
 * 
 * @see <a href="https://en.wikipedia.org/wiki/AVL_tree">AVL tree on Wikipedia</a>
 * @param <T> Type of the tree's elements.
 */
public class AVLTree<T extends Comparable<T>> implements Collection<T> {

  private AVLNode root;

  private int size = 0;

  /**
   * Finds the first greater element to given one.<br>
   * <br>
   * More formal: Returned value is the minimum by {@link Comparable#compareTo(Object)} comparison
   * of the set: {@code { x in tree | x.compareTo(toCompare) > 0 }}
   * 
   * @param toCompare Element to compare by.
   * @return Minimal greater element.
   */
  public T findGreater(T toCompare) {
    if (root == null) {
      return null;
    }

    AVLNode current = root;
    AVLNode best = null;
    while (true) {
      if (current == null) {
        return best == null ? root.val : best.val;
      }

      int comparison = current.val.compareTo(toCompare);
      if (comparison == 0) {
        current = current.gtr;
      } else if (comparison > 0) {
        best = current;
        current = current.leq;
      } else {
        current = current.gtr;
      }
    }
  }

  /**
   * Returns the greatest element in the tree or {@code null} if the tree is empty.
   * 
   * @return Greatest element in the tree.
   */
  public T getGreatest() {
    if (root == null) {
      return null;
    }

    AVLNode greatest = root;
    while (greatest.gtr != null) {
      greatest = greatest.gtr;
    }

    return greatest.val;
  }

  @Override
  public int size() {
    return size;
  }

  public int height() {
    return root == null ? 0 : root.height;
  }

  @Override
  public boolean isEmpty() {
    return size() == 0;
  }

  /**
   * Same as {@link #contains(Object)} but is guaranteed to run in {@code O(log n)} with {@code n}
   * being amount of values in this tree. The default implementation of {@link #contains(Object)}
   * will run in {@code O(n)}.
   * 
   * @param value Value to check presence of.
   * @return {@code true} if and only if value is present in the tree.
   */
  public boolean contains(final T value) {
    if (root == null || value == null) {
      return false;
    }

    AVLNode node = root.traverse(value, true);
    return node != null && node.val.equals(value);
  }

  @Override
  public boolean contains(final Object o) {
    if (o == null) {
      return false;
    }

    Iterator<T> iterator = iterator();
    while (iterator.hasNext()) {
      if (iterator.next().equals(o)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Returns an iterator over all elements of the AVL tree. Iteration will be executed by
   * depth-first search. Iterator is not safe for concurrent modification and will behave undefined
   * after collection altering.
   * 
   * @see <a href="https://en.wikipedia.org/wiki/Depth-first_search">Depth-first search on
   *      Wikipedia</a>
   * @return Iterator over all elements of the AVL tree.
   */
  @Override
  public Iterator<T> iterator() {
    return new AVLIterator();
  }

  @Override
  public Object[] toArray() {
    Iterator<T> iterator = iterator();
    Object[] array = new Object[size()];
    int i = 0;
    while (iterator.hasNext()) {
      array[i++] = iterator.next();
    }

    return array;
  }

  @Override
  public <V> V[] toArray(final V[] a) {
    if (a.length < size) {
      return (V[]) toArray();
    }

    Iterator<T> iterator = iterator();
    Object[] result = a;
    int i = 0;
    while (iterator.hasNext()) {
      result[i++] = iterator.next();
    }

    if (size() < a.length) {
      a[size()] = null;
    }

    return a;
  }

  @Override
  public boolean add(final T t) {
    if (t == null) {
      throw new NullPointerException();
    }

    if (root == null) {
      root = new AVLNode(t, null);
      size++;
      return true;
    }

    root.traverse(t, false).add(t);
    size++;
    root = root.root();
    return true;
  }

  /**
   * Same as {@link #remove(Object)} but is guaranteed to run in {@code O(log n)} with {@code n}
   * being amount of values in this tree. The default implementation of {@link #remove(Object)} will
   * run in {@code O(n)}.
   * 
   * @param t Value to remove.
   * @return {@code true} if and only if value is present in the tree.
   */
  public boolean remove(final T t) {
    if (root == null || t == null) {
      return false;
    }

    if (root.val.equals(t)) {
      root = merge(root.leq, root.gtr);
      size--;
      return true;
    }

    AVLNode toRemove = root.traverse(t, true);
    if (toRemove.val.equals(t)) {
      toRemove.parent.remove(toRemove);
      root = root.root();
      size--;
      return true;
    }

    return false;
  }

  @Override
  public boolean remove(final Object o) {
    if (o == null) {
      return false;
    }

    AVLIterator iterator = new AVLIterator();
    while (iterator.hasNext()) {
      AVLNode node = iterator.nextNode();
      if (node.val.equals(o)) {
        node.parent.remove(node);
        root = root.root();
        size--;
        return true;
      }
    }

    return false;
  }

  @Override
  public boolean containsAll(final Collection<?> c) {
    return c.stream().allMatch(this::contains);
  }

  @Override
  public boolean addAll(final Collection<? extends T> c) {
    boolean changed = false;
    for (T element : c) {
      changed |= add(element);
    }

    return changed;
  }

  @Override
  public boolean removeAll(final Collection<?> c) {
    boolean changed = false;
    for (Object element : c) {
      changed |= remove(element);
    }
    return changed;
  }

  @Override
  public boolean retainAll(final Collection<?> c) {
    List<T> toRemove = new LinkedList<>();
    Iterator<T> iterator = new AVLIterator();
    while (iterator.hasNext()) {
      T next = iterator.next();
      if (!c.contains(next)) {
        toRemove.add(next);
      }
    }

    toRemove.forEach(this::remove);

    return !toRemove.isEmpty();
  }

  @Override
  public void clear() {
    root = null;
    size = 0;
  }

  private AVLNode merge(AVLNode tree1, AVLNode tree2) {
    if (tree1 != null) {
      tree1.parent = null;
    }

    if (tree2 != null) {
      tree2.parent = null;
    }

    if (tree1 == null) {
      return tree2;
    }

    if (tree2 == null) {
      return tree1;
    }

    tree1.traverse(tree2, false).addTree(tree2);
    return tree1.root();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }

    if (obj == this) {
      return true;
    }

    if (!(obj instanceof AVLTree)) {
      return false;
    }

    Iterator iterator = ((AVLTree) obj).iterator();
    Iterator<T> thisIterator = iterator();
    while (iterator.hasNext()) {
      if (!thisIterator.hasNext()) {
        return false;
      }

      if (!iterator.next().equals(thisIterator.next())) {
        return false;
      }
    }

    return !thisIterator.hasNext();
  }

  /**
   * Wrapper class for a node in the AVL tree.
   */
  private class AVLNode {

    private T val;

    private AVLNode parent;

    private AVLNode leq = null;

    private AVLNode gtr = null;

    private int balanceFactor = 0;

    private int height = 1;

    AVLNode(final T val, final AVLNode parent) {
      this.val = val;
      this.parent = parent;
    }

    /**
     * Traverses the AVL tree looking for {@code toFind.val}. Further information at
     * {@link #traverse(Comparable, boolean)}.
     * 
     * @param toFind Element to look for.
     * @param lookup Traverse parameter.
     * @return Search result.
     */
    AVLNode traverse(final AVLNode toFind, final boolean lookup) {
      return traverse(toFind.val, lookup);
    }

    /**
     * Traverses the AVL tree looking for {@code toFind}. If {@code lookup} is {@code true} a search
     * for an AVL node containing {@code toFind} will be performed. If {@code lookup} is
     * {@code false} traversal will search for a leaf to insert {@code toFind}.
     * 
     * @param toFind Value to look for.
     * @param lookup Traverse parameter.
     * @return Search result.
     */
    AVLNode traverse(final T toFind, final boolean lookup) {
      for (AVLNode traversed = this;;) {
        if (lookup && traversed.val.equals(toFind)) {
          return traversed;
        }

        AVLNode tmp = traversed.val.compareTo(toFind) > 0 ? traversed.leq : traversed.gtr;
        if (tmp == null) {
          if (lookup) {
            return null;
          }
          return traversed;
        }
        traversed = tmp;
      }
    }

    /**
     * Returns the root of the node.
     * 
     * @return Root.
     */
    AVLNode root() {
      AVLNode node = this;
      while (node.parent != null) {
        node = node.parent;
      }
      return node;
    }

    /**
     * Adds a value to this AVL node as direct child.
     * 
     * @param value Value to add.
     */
    void add(final T value) {
      addTree(new AVLNode(value, this));
    }

    /**
     * Adds a tree to the AVL node as direct child.
     * 
     * @param toAdd Tree to add.
     */
    void addTree(final AVLNode toAdd) {
      if (val.compareTo(toAdd.val) > 0) {
        if (leq != null)
          throw new IllegalStateException();
        leq = toAdd;
      } else {
        if (gtr != null)
          throw new IllegalStateException();
        gtr = toAdd;
      }
      toAdd.parent = this;

      checkBalance();
    }

    /**
     * Removes a direct child from this node.
     * 
     * @param toRemove Child to remove.
     */
    void remove(final AVLNode toRemove) {
      AVLNode merged = merge(toRemove.gtr, toRemove.leq);
      if (merged != null) {
        merged.parent = this;
      }

      if (leq == toRemove) {
        leq = merged;
      } else if (gtr == toRemove) {
        gtr = merged;
      } else {
        throw new IllegalArgumentException();
      }
    }

    /**
     * Refreshes the {@link #balanceFactor}.
     */
    void refreshBalance() {
      int gtrHeight = gtr == null ? 0 : gtr.height;
      int leqHeight = leq == null ? 0 : leq.height;
      height = Math.max(gtrHeight, leqHeight) + 1;
      balanceFactor = gtrHeight - leqHeight;
    }

    void checkBalance() {
      refreshBalance();
      if (Math.abs(balanceFactor) > 1) {
        rebalance();
      }

      if (balanceFactor != 0 && parent != null) {
        parent.checkBalance();
      }
    }

    /**
     * Performs a rebalance of the sub-tree with this node as root by invocation
     * {@link #rotateLeft()} and/or {@link #rotateRight()}.
     */
    void rebalance() {
      if (balanceFactor < 0) {
        if ((leq.gtr == null ? 0 : leq.gtr.height) <= (leq.leq == null ? 0 : leq.leq.height)) {
          rotateRight();
        } else {
          leq.rotateLeft();
          rotateRight();
        }
      } else {
        if ((gtr.leq == null ? 0 : gtr.leq.height) <= (gtr.gtr == null ? 0 : gtr.gtr.height)) {
          rotateLeft();
        } else {
          gtr.rotateRight();
          rotateLeft();
        }
      }
    }

    /**
     * Performs a left rotation on the sub-tree with this node as root.
     */
    void rotateLeft() {
      gtr.parent = parent;
      if (parent != null) {
        if (parent.leq == this) {
          parent.leq = gtr;
        } else {
          parent.gtr = gtr;
        }
      }

      AVLNode gtrLeq = gtr.leq;
      gtr.leq = this;
      parent = gtr;

      gtr = gtrLeq;
      if (gtr != null) {
        gtr.parent = this;
      }

      refreshBalance();
      if (parent != null) {
        parent.refreshBalance();
      }
    }

    /**
     * Performs a right rotation on the sub-tree with this node as root.
     */
    void rotateRight() {
      leq.parent = parent;
      if (parent != null) {
        if (parent.leq == this) {
          parent.leq = leq;
        } else {
          parent.gtr = leq;
        }
      }

      AVLNode leqGtr = leq.gtr;
      leq.gtr = this;
      parent = leq;

      leq = leqGtr;
      if (leq != null) {
        leq.parent = this;
      }

      refreshBalance();
      if (parent != null) {
        parent.refreshBalance();
      }
    }

    @Override
    public String toString() {
      return val.toString();
    }
  }

  /**
   * Iterator over all elements of this collection. Executed by depth first search.
   */
  private class AVLIterator implements Iterator<T> {

    /**
     * Index of the next node to output in {@link #traverseArray}.
     */
    private int traverseIndex = -1;

    /**
     * Saves the path to next output node.
     */
    private Object[] traverseArray = new Object[height()];

    AVLIterator() {
      if (root != null) {
        traverseArray[++traverseIndex] = root;
      }
    }

    @Override
    public boolean hasNext() {
      // other || (init check)
      return traverseIndex > -1;
    }

    @Override
    public T next() {
      return nextNode().val;
    }

    AVLNode nextNode() {
      if (traverseIndex == -1) {
        return null;
      }

      AVLNode output = (AVLNode) traverseArray[traverseIndex];
      setNext();

      return output;
    }

    /**
     * Sets the next node to output in {@link #traverseArray}.
     */
    private void setNext() {
      // while (true) {...} mimics recursion without filling up the stack
      while (true) {
        if (traverseIndex == -1) {
          return;
        }

        traverseIndex++;

        if (traverseIndex == traverseArray.length) {
          traverseIndex -= 2;
          continue; // "recursive" call
        }

        AVLNode parent = (AVLNode) traverseArray[traverseIndex - 1];

        if (parent.leq == null && parent.gtr == null) {
          traverseIndex -= 2;
          continue; // "recursive" call
        }

        AVLNode outputChild = (AVLNode) traverseArray[traverseIndex];

        if (outputChild == null || (outputChild != parent.leq && outputChild != parent.gtr)) {
          AVLNode next = parent.leq != null ? parent.leq : parent.gtr;
          traverseArray[traverseIndex] = next;
          return;
        } else if (outputChild == parent.leq && parent.gtr != null) {
          traverseArray[traverseIndex] = parent.gtr;
          return;
        }

        traverseIndex -= 2;
        // "recursive" call
      }
    }
  }
}

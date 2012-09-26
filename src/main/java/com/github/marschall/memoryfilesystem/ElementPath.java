  package com.github.marschall.memoryfilesystem;

import static java.lang.Math.min;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

abstract class ElementPath extends AbstractPath {
  
  private final List<String> nameElements;

  ElementPath(MemoryFileSystem fileSystem, List<String> nameElements) {
    super(fileSystem);
    this.nameElements = nameElements;
  }
  
  List<String> getNameElements() {
    return this.nameElements;
  }
  
  String getNameElement(int index) {
    return this.nameElements.get(index);
  }
  
  String getLastNameElement() {
    return this.nameElements.get(this.nameElements.size() - 1);
  }


  @Override
  public Path getFileName() {
    if (this.nameElements.isEmpty()) {
      //REVIEW can this really happen?
      return null;
    } else {
      String lastElement = nameElements.get(nameElements.size() - 1);
      List<String> elements = Collections.singletonList(lastElement);
      return new RelativePath(getMemoryFileSystem(), elements);
    }
  }
  
  @Override
  boolean isRoot() {
    return false;
  }


  @Override
  public int getNameCount() {
    return this.nameElements.size();
  }


  @Override
  public boolean startsWith(String other) {
    Path path = this.getMemoryFileSystem().getPath(other);
    return this.startsWith(path);
  }


  @Override
  public boolean endsWith(String other) {
    Path path = this.getMemoryFileSystem().getPath(other);
    return this.endsWith(path);
  }


  @Override
  public Iterator<Path> iterator() {
    return new ElementIterator(getMemoryFileSystem(), this.nameElements.iterator());
  }
  
  protected int firstDifferenceIndex(List<?> l1, List<?> l2) {
    int endIndex = min(l1.size(), l2.size());
    for (int i = 0; i < endIndex; ++i) {
      if (!l1.get(i).equals(l2.get(i))) {
        return i;
      }
    }
    return endIndex;
  }

  Path buildRelativePathAgainst(AbstractPath other) {
    ElementPath otherPath = (ElementPath) other;
    int firstDifferenceIndex = firstDifferenceIndex(this.getNameElements(), otherPath.getNameElements());
    List<String> first = Collections.emptyList();
    if (firstDifferenceIndex < this.getNameCount()) {
      first = HomogenousList.create("..", this.getNameCount() - firstDifferenceIndex);
    }
    List<String> second = Collections.emptyList();
    if (firstDifferenceIndex < other.getNameCount()) {
      second = otherPath.getNameElements().subList(firstDifferenceIndex, otherPath.getNameCount());
    }
    List<String> relativeElements = CompositeList.create(first, second);
    return new RelativePath(this.getMemoryFileSystem(), relativeElements);
  }

  static final class ElementIterator implements Iterator<Path> {
    
    private final MemoryFileSystem fileSystem;
    private final Iterator<String> nameIterator;
    
    ElementIterator(MemoryFileSystem fileSystem, Iterator<String> nameIterator) {
      this.fileSystem = fileSystem;
      this.nameIterator = nameIterator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext() {
      return this.nameIterator.hasNext();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path next() {
      List<String> elements = Collections.singletonList(this.nameIterator.next());
      return new RelativePath(fileSystem, elements);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove() {
      throw new UnsupportedOperationException("can't remove from a path iterator");
    }
    
  } 

}
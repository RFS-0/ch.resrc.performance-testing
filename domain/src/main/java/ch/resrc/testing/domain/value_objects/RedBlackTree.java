package ch.resrc.testing.domain.value_objects;

import ch.resrc.testing.capabilities.error_handling.ProblemDiagnosis;
import ch.resrc.testing.capabilities.error_handling.faults.OurFault;
import io.vavr.*;
import io.vavr.collection.Iterator;
import io.vavr.collection.List;
import io.vavr.control.Option;

import java.io.Serializable;
import java.lang.Iterable;
import java.util.*;

import static ch.resrc.testing.domain.value_objects.RedBlackTree.Color.*;

public interface RedBlackTree<T> extends Iterable<T> {

    static <T> RedBlackTree<T> empty(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator, "comparator is null");
        return new RedBlackTreeModule.Empty<>(comparator);
    }

    static <T> RedBlackTree<T> of(Comparator<? super T> comparator, T value) {
        Objects.requireNonNull(comparator, "comparator is null");
        final RedBlackTreeModule.Empty<T> empty = new RedBlackTreeModule.Empty<>(comparator);
        return new RedBlackTreeModule.Node<>(BLACK, 1, empty, value, empty, empty);
    }

    @SafeVarargs
    static <T> RedBlackTree<T> of(Comparator<? super T> comparator, T... values) {
        Objects.requireNonNull(comparator, "comparator is null");
        Objects.requireNonNull(values, "values is null");
        RedBlackTree<T> tree = empty(comparator);
        for (T value : values) {
            tree = tree.insert(value);
        }
        return tree;
    }

    @SuppressWarnings("unchecked")
    static <T> RedBlackTree<T> ofAll(Comparator<? super T> comparator, Iterable<? extends T> values) {
        Objects.requireNonNull(comparator, "comparator is null");
        Objects.requireNonNull(values, "values is null");
        // function equality is not computable => same object check
        if (values instanceof RedBlackTree && ((RedBlackTree<T>) values).comparator() == comparator) {
            return (RedBlackTree<T>) values;
        } else {
            RedBlackTree<T> tree = empty(comparator);
            for (T value : values) {
                tree = tree.insert(value);
            }
            return tree;
        }
    }

    default RedBlackTree<T> insert(T value) {
        return RedBlackTreeModule.Node.insert(this, value).color(BLACK);
    }

    Color color();

    Comparator<T> comparator();

    boolean contains(T value);

    RedBlackTree<T> emptyInstance();

    Option<T> find(T value);

    boolean isEmpty();

    RedBlackTree<T> left();

    RedBlackTree<T> right();

    int size();

    T value();

    @Override
    default Iterator<T> iterator() {
        if (isEmpty()) {
            return Iterator.empty();
        } else {
            final RedBlackTreeModule.Node<T> that = (RedBlackTreeModule.Node<T>) this;
            return new Iterator<T>() {

                List<RedBlackTreeModule.Node<T>> stack = pushLeftChildren(List.empty(), that);

                @Override
                public boolean hasNext() {
                    return !stack.isEmpty();
                }

                @Override
                public T next() {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    final Tuple2<RedBlackTreeModule.Node<T>, ? extends List<RedBlackTreeModule.Node<T>>> result = stack.pop2();
                    final RedBlackTreeModule.Node<T> node = result._1;
                    stack = node.right.isEmpty() ? result._2 : pushLeftChildren(result._2, (RedBlackTreeModule.Node<T>) node.right);
                    return result._1.value;
                }

                private List<RedBlackTreeModule.Node<T>> pushLeftChildren(List<RedBlackTreeModule.Node<T>> initialStack, RedBlackTreeModule.Node<T> that) {
                    List<RedBlackTreeModule.Node<T>> stack = initialStack;
                    RedBlackTree<T> tree = that;
                    while (!tree.isEmpty()) {
                        final RedBlackTreeModule.Node<T> node = (RedBlackTreeModule.Node<T>) tree;
                        stack = stack.push(node);
                        tree = node.left;
                    }
                    return stack;
                }
            };
        }
    }

    enum Color {

        RED, BLACK;

        @Override
        public String toString() {
            return (this == RED) ? "R" : "B";
        }
    }
}

interface RedBlackTreeModule {

    final class Empty<T> implements RedBlackTree<T>, Serializable {

        private static final long serialVersionUID = 1L;

        final Comparator<T> comparator;

        @SuppressWarnings("unchecked")
        Empty(Comparator<? super T> comparator) {
            this.comparator = (Comparator<T>) comparator;
        }

        @Override
        public Color color() {
            return BLACK;
        }

        @Override
        public Comparator<T> comparator() {
            return comparator;
        }

        @Override
        public boolean contains(T value) {
            return false;
        }

        @Override
        public RedBlackTree<T> emptyInstance() {
            return this;
        }

        @Override
        public Option<T> find(T value) {
            return Option.none();
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public RedBlackTree<T> left() {
            throw OurFault.of(ProblemDiagnosis.of("left on empty"));
        }

        @Override
        public RedBlackTree<T> right() {
            throw OurFault.of(ProblemDiagnosis.of("left on empty"));
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public T value() {
            throw OurFault.of(ProblemDiagnosis.of("value on empty"));
        }

        @Override
        public String toString() {
            return "()";
        }
    }

    final class Node<T> implements RedBlackTree<T>, Serializable {

        private static final long serialVersionUID = 1L;

        final Color color;
        final int blackHeight;
        final RedBlackTree<T> left;
        final T value;
        final RedBlackTree<T> right;
        final Empty<T> empty;
        final int size;

        Node(Color color, int blackHeight, RedBlackTree<T> left, T value, RedBlackTree<T> right, Empty<T> empty) {
            this.color = color;
            this.blackHeight = blackHeight;
            this.left = left;
            this.value = value;
            this.right = right;
            this.empty = empty;
            this.size = left.size() + right.size() + 1;
        }

        @Override
        public Color color() {
            return color;
        }

        @Override
        public Comparator<T> comparator() {
            return empty.comparator;
        }

        @Override
        public boolean contains(T value) {
            final int result = empty.comparator.compare(value, this.value);
            if (result < 0) {
                return left.contains(value);
            } else if (result > 0) {
                return right.contains(value);
            } else {
                return true;
            }
        }

        @Override
        public RedBlackTree<T> emptyInstance() {
            return empty;
        }

        @Override
        public Option<T> find(T value) {
            final int result = empty.comparator.compare(value, this.value);
            if (result < 0) {
                return left.find(value);
            } else if (result > 0) {
                return right.find(value);
            } else {
                return Option.some(value);
            }
        }

        @Override
        public Iterator<T> iterator() {
            if (isEmpty()) {
                return Iterator.empty();
            } else {
                final Node<T> that = this;
                return new Iterator<T>() {

                    List<Node<T>> stack = pushLeftChildren(List.empty(), that);

                    @Override
                    public boolean hasNext() {
                        return !stack.isEmpty();
                    }

                    @Override
                    public T next() {
                        if (!hasNext()) {
                            throw new NoSuchElementException();
                        }
                        final Tuple2<Node<T>, ? extends List<Node<T>>> result = stack.pop2();
                        final Node<T> node = result._1;
                        stack = node.right.isEmpty() ? result._2 : pushLeftChildren(result._2, (Node<T>) node.right);
                        return result._1.value;
                    }

                    private List<Node<T>> pushLeftChildren(List<Node<T>> initialStack, Node<T> that) {
                        List<Node<T>> stack = initialStack;
                        RedBlackTree<T> tree = that;
                        while (!tree.isEmpty()) {
                            final Node<T> node = (Node<T>) tree;
                            stack = stack.push(node);
                            tree = node.left;
                        }
                        return stack;
                    }
                };
            }
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public RedBlackTree<T> left() {
            return left;
        }

        @Override
        public RedBlackTree<T> right() {
            return right;
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public T value() {
            return value;
        }

        private boolean isLeaf() {
            return left.isEmpty() && right.isEmpty();
        }

        @Override
        public String toString() {
            return isLeaf() ? "(" + color + ":" + value + ")" : toLispString(this);
        }

        private static String toLispString(RedBlackTree<?> tree) {
            if (tree.isEmpty()) {
                return "";
            } else {
                final Node<?> node = (Node<?>) tree;
                final String value = node.color + ":" + node.value;
                if (node.isLeaf()) {
                    return value;
                } else {
                    final String left = toLispString(node.left);
                    final String right = toLispString(node.right);
                    return "(" + value + left + right + ")";
                }
            }
        }

        Node<T> color(Color color) {
            return (this.color == color) ? this : new Node<>(color, blackHeight, left, value, right, empty);
        }

        static <T> RedBlackTree<T> color(RedBlackTree<T> tree, Color color) {
            return tree.isEmpty() ? tree : ((Node<T>) tree).color(color);
        }

        private static <T> Node<T> balanceLeft(Color color, int blackHeight, RedBlackTree<T> left, T value,
                                               RedBlackTree<T> right, Empty<T> empty) {
            if (color == BLACK) {
                if (!left.isEmpty()) {
                    final Node<T> ln = (Node<T>) left;
                    if (ln.color == RED) {
                        if (!ln.left.isEmpty()) {
                            final Node<T> lln = (Node<T>) ln.left;
                            if (lln.color == RED) {
                                final Node<T> newLeft = new Node<>(BLACK, blackHeight, lln.left, lln.value, lln.right,
                                        empty);
                                final Node<T> newRight = new Node<>(BLACK, blackHeight, ln.right, value, right, empty);
                                return new Node<>(RED, blackHeight + 1, newLeft, ln.value, newRight, empty);
                            }
                        }
                        if (!ln.right.isEmpty()) {
                            final Node<T> lrn = (Node<T>) ln.right;
                            if (lrn.color == RED) {
                                final Node<T> newLeft = new Node<>(BLACK, blackHeight, ln.left, ln.value, lrn.left,
                                        empty);
                                final Node<T> newRight = new Node<>(BLACK, blackHeight, lrn.right, value, right, empty);
                                return new Node<>(RED, blackHeight + 1, newLeft, lrn.value, newRight, empty);
                            }
                        }
                    }
                }
            }
            return new Node<>(color, blackHeight, left, value, right, empty);
        }

        private static <T> Node<T> balanceRight(Color color, int blackHeight, RedBlackTree<T> left, T value,
                                                RedBlackTree<T> right, Empty<T> empty) {
            if (color == BLACK) {
                if (!right.isEmpty()) {
                    final Node<T> rn = (Node<T>) right;
                    if (rn.color == RED) {
                        if (!rn.right.isEmpty()) {
                            final Node<T> rrn = (Node<T>) rn.right;
                            if (rrn.color == RED) {
                                final Node<T> newLeft = new Node<>(BLACK, blackHeight, left, value, rn.left, empty);
                                final Node<T> newRight = new Node<>(BLACK, blackHeight, rrn.left, rrn.value, rrn.right,
                                        empty);
                                return new Node<>(RED, blackHeight + 1, newLeft, rn.value, newRight, empty);
                            }
                        }
                        if (!rn.left.isEmpty()) {
                            final Node<T> rln = (Node<T>) rn.left;
                            if (rln.color == RED) {
                                final Node<T> newLeft = new Node<>(BLACK, blackHeight, left, value, rln.left, empty);
                                final Node<T> newRight = new Node<>(BLACK, blackHeight, rln.right, rn.value, rn.right,
                                        empty);
                                return new Node<>(RED, blackHeight + 1, newLeft, rln.value, newRight, empty);
                            }
                        }
                    }
                }
            }
            return new Node<>(color, blackHeight, left, value, right, empty);
        }

        private static <T> Tuple2<? extends RedBlackTree<T>, Boolean> blackify(RedBlackTree<T> tree) {
            if (tree instanceof Node) {
                final Node<T> node = (Node<T>) tree;
                if (node.color == RED) {
                    return Tuple.of(node.color(BLACK), false);
                }
            }
            return Tuple.of(tree, true);
        }

        static <T> Tuple2<? extends RedBlackTree<T>, Boolean> delete(RedBlackTree<T> tree, T value) {
            if (tree.isEmpty()) {
                return Tuple.of(tree, false);
            } else {
                final Node<T> node = (Node<T>) tree;
                final int comparison = node.comparator().compare(value, node.value);
                if (comparison < 0) {
                    final Tuple2<? extends RedBlackTree<T>, Boolean> deleted = delete(node.left, value);
                    final RedBlackTree<T> l = deleted._1;
                    final boolean d = deleted._2;
                    if (d) {
                        return Node.unbalancedRight(node.color, node.blackHeight - 1, l, node.value, node.right,
                                node.empty);
                    } else {
                        final Node<T> newNode = new Node<>(node.color, node.blackHeight, l, node.value, node.right,
                                node.empty);
                        return Tuple.of(newNode, false);
                    }
                } else if (comparison > 0) {
                    final Tuple2<? extends RedBlackTree<T>, Boolean> deleted = delete(node.right, value);
                    final RedBlackTree<T> r = deleted._1;
                    final boolean d = deleted._2;
                    if (d) {
                        return Node.unbalancedLeft(node.color, node.blackHeight - 1, node.left, node.value, r,
                                node.empty);
                    } else {
                        final Node<T> newNode = new Node<>(node.color, node.blackHeight, node.left, node.value, r,
                                node.empty);
                        return Tuple.of(newNode, false);
                    }
                } else {
                    if (node.right.isEmpty()) {
                        if (node.color == BLACK) {
                            return blackify(node.left);
                        } else {
                            return Tuple.of(node.left, false);
                        }
                    } else {
                        final Node<T> nodeRight = (Node<T>) node.right;
                        final Tuple3<? extends RedBlackTree<T>, Boolean, T> newRight = deleteMin(nodeRight);
                        final RedBlackTree<T> r = newRight._1;
                        final boolean d = newRight._2;
                        final T m = newRight._3;
                        if (d) {
                            return Node.unbalancedLeft(node.color, node.blackHeight - 1, node.left, m, r, node.empty);
                        } else {
                            final RedBlackTree<T> newNode = new Node<>(node.color, node.blackHeight, node.left, m, r,
                                    node.empty);
                            return Tuple.of(newNode, false);
                        }
                    }
                }
            }
        }

        private static <T> Tuple3<? extends RedBlackTree<T>, Boolean, T> deleteMin(Node<T> node) {
            if (node.color() == BLACK && node.left().isEmpty() && node.right.isEmpty()) {
                return Tuple.of(node.empty, true, node.value());
            } else if (node.color() == BLACK && node.left().isEmpty() && node.right().color() == RED) {
                return Tuple.of(((Node<T>) node.right()).color(BLACK), false, node.value());
            } else if (node.color() == RED && node.left().isEmpty()) {
                return Tuple.of(node.right(), false, node.value());
            } else {
                final Node<T> nodeLeft = (Node<T>) node.left;
                final Tuple3<? extends RedBlackTree<T>, Boolean, T> newNode = deleteMin(nodeLeft);
                final RedBlackTree<T> l = newNode._1;
                final boolean deleted = newNode._2;
                final T m = newNode._3;
                if (deleted) {
                    final Tuple2<Node<T>, Boolean> tD = Node.unbalancedRight(node.color, node.blackHeight - 1, l,
                            node.value, node.right, node.empty);
                    return Tuple.of(tD._1, tD._2, m);
                } else {
                    final Node<T> tD = new Node<>(node.color, node.blackHeight, l, node.value, node.right, node.empty);
                    return Tuple.of(tD, false, m);
                }
            }
        }

        static <T> Node<T> insert(RedBlackTree<T> tree, T value) {
            if (tree.isEmpty()) {
                final Empty<T> empty = (Empty<T>) tree;
                return new Node<>(RED, 1, empty, value, empty, empty);
            } else {
                final Node<T> node = (Node<T>) tree;
                final int comparison = node.comparator().compare(value, node.value);
                if (comparison < 0) {
                    final Node<T> newLeft = insert(node.left, value);
                    return (newLeft == node.left)
                            ? node
                            : Node.balanceLeft(node.color, node.blackHeight, newLeft, node.value, node.right,
                            node.empty);
                } else if (comparison > 0) {
                    final Node<T> newRight = insert(node.right, value);
                    return (newRight == node.right)
                            ? node
                            : Node.balanceRight(node.color, node.blackHeight, node.left, node.value, newRight,
                            node.empty);
                } else {
                    // DEV-NOTE: Even if there is no _comparison_ difference, the object may not be _equal_.
                    //           To save an equals() call, which may be expensive, we return a new instance.
                    return new Node<>(node.color, node.blackHeight, node.left, value, node.right, node.empty);
                }
            }
        }

        private static boolean isRed(RedBlackTree<?> tree) {
            return !tree.isEmpty() && ((Node<?>) tree).color == RED;
        }

        static <T> T maximum(Node<T> node) {
            Node<T> curr = node;
            while (!curr.right.isEmpty()) {
                curr = (Node<T>) curr.right;
            }
            return curr.value;
        }

        static <T> T minimum(Node<T> node) {
            Node<T> curr = node;
            while (!curr.left.isEmpty()) {
                curr = (Node<T>) curr.left;
            }
            return curr.value;
        }


        private static <T> Tuple2<Node<T>, Boolean> unbalancedLeft(Color color, int blackHeight, RedBlackTree<T> left,
                                                                   T value, RedBlackTree<T> right, Empty<T> empty) {
            if (!left.isEmpty()) {
                final Node<T> ln = (Node<T>) left;
                if (ln.color == BLACK) {
                    final Node<T> newNode = Node.balanceLeft(BLACK, blackHeight, ln.color(RED), value, right, empty);
                    return Tuple.of(newNode, color == BLACK);
                } else if (color == BLACK && !ln.right.isEmpty()) {
                    final Node<T> lrn = (Node<T>) ln.right;
                    if (lrn.color == BLACK) {
                        final Node<T> newRightNode = Node.balanceLeft(BLACK, blackHeight, lrn.color(RED), value, right,
                                empty);
                        final Node<T> newNode = new Node<>(BLACK, ln.blackHeight, ln.left, ln.value, newRightNode,
                                empty);
                        return Tuple.of(newNode, false);
                    }
                }
            }
            throw new IllegalStateException("unbalancedLeft(" + color + ", " + blackHeight + ", " + left + ", " + value + ", " + right + ")");
        }

        private static <T> Tuple2<Node<T>, Boolean> unbalancedRight(Color color, int blackHeight, RedBlackTree<T> left,
                                                                    T value, RedBlackTree<T> right, Empty<T> empty) {
            if (!right.isEmpty()) {
                final Node<T> rn = (Node<T>) right;
                if (rn.color == BLACK) {
                    final Node<T> newNode = Node.balanceRight(BLACK, blackHeight, left, value, rn.color(RED), empty);
                    return Tuple.of(newNode, color == BLACK);
                } else if (color == BLACK && !rn.left.isEmpty()) {
                    final Node<T> rln = (Node<T>) rn.left;
                    if (rln.color == BLACK) {
                        final Node<T> newLeftNode = Node.balanceRight(BLACK, blackHeight, left, value, rln.color(RED),
                                empty);
                        final Node<T> newNode = new Node<>(BLACK, rn.blackHeight, newLeftNode, rn.value, rn.right,
                                empty);
                        return Tuple.of(newNode, false);
                    }
                }
            }
            throw new IllegalStateException("unbalancedRight(" + color + ", " + blackHeight + ", " + left + ", " + value + ", " + right + ")");
        }
    }
}
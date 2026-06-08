public class MyArrayList<E> {
    private Object[] data;
    private int size;

    public MyArrayList() {
        data = new Object[16];
        size = 0;
    }

    public void add(E e) {
        if (size == data.length) {
            Object[] newData = new Object[data.length * 2];
            for (int i = 0; i < size; i++) newData[i] = data[i];
            data = newData;
        }
        data[size++] = e;
    }

    @SuppressWarnings("unchecked")
    public E get(int index) {
        if (index < 0 || index >= size) throw new IndexOutOfBoundsException();
        return (E) data[index];
    }

    public boolean contains(Object o) {
        for (int i = 0; i < size; i++) {
            if (data[i].equals(o)) return true;
        }
        return false;
    }

    public int size() {
        return size;
    }
}
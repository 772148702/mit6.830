package simpledb;

import java.io.*;
import java.util.*;

/**
 * HeapFile is an implementation of a DbFile that stores a collection of tuples
 * in no particular order. Tuples are stored on pages, each of which is a fixed
 * size, and the file is simply a collection of those pages. HeapFile works
 * closely with HeapPage. The format of HeapPages is described in the HeapPage
 * constructor.
 * 
 * @see simpledb.HeapPage#HeapPage
 * @author Sam Madden
 */
public class HeapFile implements DbFile {

    //应该是一张表了
    File f;
    TupleDesc td;


    /**
     * Constructs a heap file backed by the specified file.
     * 
     * @param f
     *            the file that stores the on-disk backing store for this heap
     *            file.
     */


    public HeapFile(File f, TupleDesc td) {
        // some code goes here
        this.f = f;
        this.td = td;
    }

    /**
     * Returns the File backing this HeapFile on disk.
     * 
     * @return the File backing this HeapFile on disk.
     */
    public File getFile() {
        // some code goes here
        return f;
    }

    /**
     * Returns an ID uniquely identifying this HeapFile. Implementation note:
     * you will need to generate this tableid somewhere to ensure that each
     * HeapFile has a "unique id," and that you always return the same value for
     * a particular HeapFile. We suggest hashing the absolute file name of the
     * file underlying the heapfile, i.e. f.getAbsoluteFile().hashCode().
     * 
     * @return an ID uniquely identifying this HeapFile.
     */
    public int getId() {
        // some code goes here
        return f.getAbsoluteFile().hashCode();
//       throw new UnsupportedOperationException("implement this");
    }

    /**
     * Returns the TupleDesc of the table stored in this DbFile.
     * 
     * @return TupleDesc of this DbFile.
     */
    public TupleDesc getTupleDesc() {
        // some code goes here
        return td;
//        throw new UnsupportedOperationException("implement this");
    }

    // see DbFile.java for javadocs
    public Page readPage(PageId pid) {
        // some code goes here

        if (getId() == pid.getTableId()) {
            int pgNo = pid.getPageNumber();

            if (pgNo>=0 && pgNo<numPages()) {
                byte[] bytes = HeapPage.createEmptyPageData();

                try {
                    RandomAccessFile raf = new RandomAccessFile(f, "r");

                    try {
                        raf.seek(1L*BufferPool.getPageSize()*pid.getPageNumber());
                        raf.read(bytes, 0, BufferPool.getPageSize());
                        return new HeapPage(HeapPageId.valueOf(pid), bytes);
                    } finally {
                        raf.close();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        throw new IllegalArgumentException("page not in the file");
    }

    // see DbFile.java for javadocs
    public void writePage(Page page) throws IOException {
        // some code goes here
        // not necessary for lab1
    }

    /**
     * Returns the number of pages in this HeapFile.
     */
    public int numPages() {
        // some code goes here
        return (int)(f.length()/BufferPool.getPageSize());
    }

    // see DbFile.java for javadocs
    public ArrayList<Page> insertTuple(TransactionId tid, Tuple t)
            throws DbException, IOException, TransactionAbortedException {
        // some code goes here
        return null;
        // not necessary for lab1
    }

    // see DbFile.java for javadocs
    public ArrayList<Page> deleteTuple(TransactionId tid, Tuple t) throws DbException,
            TransactionAbortedException {
        // some code goes here
        return null;
        // not necessary for lab1
    }

    // see DbFile.java for javadocs
    public DbFileIterator iterator(TransactionId tid) {
        // some code goes here
        return new DbFileIterator() {

            private final BufferPool pool = Database.getBufferPool();
            private final int tableId = getId();
            private int pid = -1;
            private Iterator<Tuple> child;

            @Override
            public void open() throws DbException, TransactionAbortedException {
                pid = 0;
                child = null;
            }
            @Override
            public boolean hasNext() throws DbException,
                    TransactionAbortedException {
                if (null != child && child.hasNext()) {
                    return true;
                } else if (pid < 0 || pid >= numPages()) {
                    return false;
                } else {
                    child = ((HeapPage)pool.getPage(tid, new HeapPageId(tableId,pid++),
                            Permissions.READ_ONLY)).iterator();
                    return hasNext();
                }
            }
            @Override
            public Tuple next() throws DbException,
                    TransactionAbortedException, NoSuchElementException {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                } else {
                    return child.next();
                }
            }
            @Override
            public void rewind() throws DbException,
                    TransactionAbortedException {
                pid = 0;
                child = null;
            }
            @Override
            public void close() {
                pid = -1;
                child = null;
            }
        };
    }

}


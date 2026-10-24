/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.impl.system;

/**
 * To handle a block in a Thread where some local parameter may be used.
 *
 * @author www.b2prodtm.info
 */
public class ThreadBlockData<D> extends ThreadBlock {

    private ThreadLocal<D> tdata;

    /**
     * delegates a ThreadLocal instance with the initial data (may be null)
     *
     * @param data the {@link ThreadLocal#initialValue() } to initialize.
     */
    public ThreadBlockData(final D data) {
        super();
        tdata = new ThreadLocal<D>() {

            @Override
            protected D initialValue() {
                return data == null ? super.initialValue() : data;
            }
        };
    }

    public ThreadBlockData() {
        this(null);
    }

    public D getTdata() {
        return tdata.get();
    }

    public void setTdata(D tdata) {
        this.tdata.set(tdata);
    }

    public void removeTdata() {
        this.tdata.remove();
    }

}

/*

 * Custom.java

 *

 * Created on 30 mai 2007, 05:06

 *

 * To change this template, choose Tools | Template Manager

 * and open the template in the editor.

 */
package net.sf.jiga.xtended.impl.game;

import java.awt.Dimension;

import java.awt.datatransfer.DataFlavor;
import java.io.Externalizable;

import java.io.IOException;
import java.io.ObjectInput;

import java.io.ObjectOutput;


import java.io.Serializable;

import java.util.Collections;


import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import jakarta.activation.FileTypeMap;

import jakarta.activation.MimetypesFileTypeMap;

/**

 * The Custom class extends the abstract class SpritesChar to implement customized animation models. Each animation listed can be linked to a String value.

 * It defines the paths to animation and sound files, as well as the various properties.

 * @see #animsMap

 * @see #sfxMap

 * @author www.b23prodtm.info

 */
public class Custom extends SpritesChar implements Externalizable {

    /** serial version UID */
    private static final long serialVersionUID = 2323;
    /**
     * the attributes map
     */
    transient Map<String, Serializable> attributes;

    /** the DataFlavor used for the mime-type by this Custom class 
    
     * @default DataFlavor.javaSerializedObjectMimeType*/
    public static final String _MIME_TYPE = DataFlavor.javaSerializedObjectMimeType;
    /** the file extensions supported by this Model class 
    
     * @default mc3 MC3*/
    public static final String[] _MIME_EXT = new String[]{"mcd", "MCD"};
    /** the DataFlavor used for identifying a Model instance */
    public static final DataFlavor _dataFlavor = new DataFlavor(Custom.class, "sf3jswing Custom");

    /** resets mapping to the selected animation to {@linkplain #_getDefaultAnimName(int) default name}*/
    public void resetToDefaultAnimName(int index) {
        addAnimMap(_getDefaultAnimName(index), index);
    }

    /** resets all mappings to {@linkplain #_getDefaultAnimName(int) default names}*/
    public final void resetAllToDefaultAnimName() {
        animsMap.clear();
        for (int i = 0; i < frames.length; i++) {
            addAnimMap(_getDefaultAnimName(i), i);
        }
    }

    /** stores the mime-type in a mime-type file
    
    @see MimetypesFileTypeMap#addMimeTypes(String)*/
    public static void _storeMIMETYPES(String _MIME_TYPE, String[] _MIME_EXT) {

        String s = _MIME_TYPE;

        for (String ext : _MIME_EXT) {

            s += " " + ext;

        }

        if (!((MimetypesFileTypeMap) FileTypeMap.getDefaultFileTypeMap()).getContentType("file." + _MIME_EXT[0]).equals(_MIME_TYPE)) {

            ((MimetypesFileTypeMap) FileTypeMap.getDefaultFileTypeMap()).addMimeTypes(s);

        }

    }

    static {

        _storeMIMETYPES(_MIME_TYPE, _MIME_EXT);

    }
    /** Maps of the animations and sound effects, all linked to animations ids
    
     * @see SpritesChar#animsID*/
    public Map<String, Integer> animsMap, sfxMap;

    /** creates a customized character or Model. the resolution "res" specified here defines a factor out of 3 existing factors provided by the super abstract class and computes all other factors with the next specified dimensions values. <pre>
    
    <span class="line-number"> 44</span>         <span class="literal">this</span>.floorDiff = (<span class="literal">int</span>)((<span class="literal">float</span>)floorDiff / RES_FACTORS_map.get(res).floatValue());
    
    <span class="line-number"> 45</span>         spriteWidth = (<span class="literal">int</span>)((<span class="literal">float</span>)spriteDim.width / RES_FACTORS_map.get(res).floatValue());
    
    <span class="line-number"> 46</span>         spriteHeight = (<span class="literal">int</span>)((<span class="literal">float</span>)spriteDim.height/ RES_FACTORS_map.get(res).floatValue());
    
    <span class="line-number"> 47</span>         <span class="literal">this</span>.spriteSize = <span class="literal">new</span> Dimension(
    
    <span class="line-number"> 48</span>                 (<span class="literal">int</span>)((<span class="literal">float</span>)spriteSize.width / RES_FACTORS_map.get(res).floatValue()),
    
    <span class="line-number"> 49</span>                 (<span class="literal">int</span>)((<span class="literal">float</span>)spriteSize.height / RES_FACTORS_map.get(res).floatValue()));</pre>
    
     * @param name character's id or base path
    
     * @param res common resolution ID of the animation sprites files. It is defined in the super class.
    
     *@see SpritesChar#RES_NAMES_map
    
     * @param floorDiff the actual floor difference in pixels from the first pixel to define as floor and the sprite bottom (height)
    
     * @see SpritesChar#floorDiff
    
     * @param spriteDim actual size of the sprites
    
     * @param spriteSize average inset size of the sprites
    
     * @param framesNum 2D-array of the frames number to access sprites files (x : first frame inclusive, y : last frame inclusive)
    
     * @param reversedAnims 1D-array of the ids (String values) of the animations of sprites to be playing-reversed
    
     * @see #animsMap
    
     * @param sfxRsrcs 1D-array of the filenames of the sound effects used with this character (base path relative)*/
    public Custom(String name, int res, int floorDiff, Dimension spriteDim, Dimension spriteSize, int[][] framesNum, String[] reversedAnims, String[] sfxRsrcs) {

        this(res);

        id = name;

        this.floorDiff = (int) Math.ceil((float) floorDiff / RES_FACTORS_map.get(res).floatValue());

        spriteWidth = (int) Math.ceil((float) spriteDim.width / RES_FACTORS_map.get(res).floatValue());

        spriteHeight = (int) Math.ceil((float) spriteDim.height / RES_FACTORS_map.get(res).floatValue());

        this.spriteSize = new Dimension(
                (int) Math.ceil((float) spriteSize.width / RES_FACTORS_map.get(res).floatValue()),
                (int) Math.ceil((float) spriteSize.height / RES_FACTORS_map.get(res).floatValue()));

        frames = framesNum;

        this.reversedAnims = reversedAnims;

        sfx = sfxRsrcs;
        resetAllToDefaultAnimName();
    }

    /** default HIGHres Custom*/
    public Custom() {
        this(HIGH);
    }

    /** creates a customized character or Model
    
    @param res common resolution ID of the animation sprites files. It is defined in the super class.
    
     *@see SpritesChar#RES_EXT_map
    
     */
    Custom(int res) {

        super();

        assert RES_NAMES_map.containsKey(res) : "the specified resolution is unknown, please set up RES_NAMES_map !";

        this.res = res;

        sfxMap = Collections.synchronizedMap(new LinkedHashMap<String, Integer>());

        animsMap = Collections.synchronizedMap(new LinkedHashMap<String, Integer>());
        attributes = Collections.synchronizedMap(new LinkedHashMap<String, Serializable>());
        resetAllToDefaultAnimName();
    }

    /** adds a new mapping to the animations that are referenced by the values of this mapping
    
     * @see #anim(String)
    
     * @see #animsMap
    
    @param anims the mapping to add*/
    public void addAnimMap(Map<String, Integer> anims) {

        animsMap.putAll(anims);

    }

    /** adds a new mapping to the sound effects that are reference by the values of this mapping
    
     * @see #sfxMap
    
    @see #sound(String)*/
    public void addSoundMap(Map<String, Integer> sfx) {

        sfxMap.putAll(sfx);

    }

    /** adds a new mapping to the animations that are referenced by the values of this mapping
    
     * @param name the String value to link with the animation
    
    @param index the index value to get the animation from the current class-map
    
    @see #animsMap
    
    @see #anim(String)*/
    public void addAnimMap(String name, int index) {
        /* DISABLED , one animation can be referenced by several different names 
         synchronized (animsMap) {
            for (Iterator<Integer> i = animsMap.values().iterator(); i.hasNext();) {
                if (i.next().equals(index)) {
                    i.remove();
                }
            }
        }*/
        animsMap.put(name, index);
    }

    /** adds a new mapping to the sounds that are referenced by the values of this mapping
    
     * @param name the String value to link with the sound
    
    @param index the index value to get the sound from the current class-map
    
    @see #sfxMap
    
    @see #sound(String)*/
    public void addSoundMap(String name, int index) {

        sfxMap.put(name, index);

    }

    /** returns the linked index value linked to this String value referencing a mapped animation in this class.
     * if the name is not identified the {@linkplain #animsID_default} value is used, and if it is not mapped though, 0 is returned.
    @param name the String value referencing a mapped animation, null can be used to specify {@linkplain #animsID_default}.
    @return the index value of the requested animation or anim({@linkplain #animsID_default}) which can return 0*/
    @Override
    public int anim(String name) {
        String animName = name == null ? animsID_default : name;
        if (animName.equals("")) {
            if (animsMap.isEmpty()) {
                return 0;
            } else {
                return animsMap.values().iterator().next();
            }
        } else {
            return animsMap.containsKey(animName) ? animsMap.get(animName) : animsMap.containsKey(animsID_default) ? animsMap.get(animsID_default) : 0;
        }
    }

    /** gives the requested animation playing-reversed status
    
    @return true or false*/
    public boolean isReversedAnim(String name) {

        for (int i = 0; i < reversedAnims.length; i++) {

            if (reversedAnims[i] instanceof String) {

                if (reversedAnims[i].equals(name)) {

                    return true;

                }

            }

        }

        return false;

    }

    /** returns the linked index value linked to this String value referencing a mapped sound in this class
    
    @param name the String value referencing a mapped sound
    
    @return the index value of the requested sound*/
    @Override
    public int sound(String name) {

        return (sfxMap.containsKey(name)) ? sfxMap.get(name) : 0;

    }

    /** defines the prefix String to all sprite files
    
    @param prefix the String prefix to use*/
    public void setPrefix(String prefix) {

        this.prefix = prefix;

    }

    /** returns the current prefix String for all sprite files
    
    @return the current prefix String*/
    public String getPrefix() {

        return prefix;

    }

    /** defines the suffix String to all sprite files
    
    @param suffix the String suffix to use*/
    public void setSuffix(String suffix) {

        this.suffix = suffix;

    }

    /** returns the current suffix String for all sprite files
    
    @return the current suffix String*/
    public String getSuffix() {

        return suffix;

    }

    /***/
    @Override
    public String toString() {

        String s = "<<floorDiff (HIGHres): " + this.floorDiff + " spriteWidth (HIGHres): " + spriteWidth + " spriteHeight (HIGHres): " + spriteHeight + " spriteSize (HIGHres): " + spriteSize + " frames : " + frames.length + ">> \n";
        s += "\n:: current resolution " + SpritesChar.RES_NAMES_map.get(res) + "\n";
        return super.toString() + " " + s;

    }

    /**
     * returns the current associated value for the specified attribute name
     * @return the associated value for the specified attribute name or null
     * @see #setAttribute(String, Serializable)
     */
    public Serializable getAttribute(String name) {
        return attributes.get(name);
    }

    /**
     * sets up an attribute in the attributes map for this Model instance. Any previous mapping for the same attribute name is overriden.
     * @param name the attribute name
     * @param value the value to attribute or null to delete
     * @return the old value is returned or null
     * @see Map#put(Object, Object)
     */
    public Serializable setAttribute(String name, Serializable value) {
        if (value == null) {
            return attributes.remove(name);
        } else {
            return attributes.put(name, value);
        }
    }

    /** checks whether it has the attribute set to a value or not.
    @see #getAttribute(String)*/
    public boolean hasAttribute(String name) {
        return attributes.containsKey(name);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        int currentPty = Thread.currentThread().getPriority();
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        super.writeExternal(out);
        synchronized (animsMap) {
            out.writeInt(animsMap.size());
            for (Iterator<Entry<String, Integer>> it = animsMap.entrySet().iterator(); it.hasNext();) {
                Entry<String, Integer> k = it.next();
                out.writeUTF(k.getKey());
                out.writeInt(k.getValue());
            }
        }
        synchronized (sfxMap) {
            out.writeInt(sfxMap.size());
            for (Iterator<Entry<String, Integer>> it = sfxMap.entrySet().iterator(); it.hasNext();) {
                Entry<String, Integer> k = it.next();
                out.writeUTF(k.getKey());
                out.writeInt(k.getValue());
            }
        }
        endWrite(out);
        out.flush();
        Thread.currentThread().setPriority(currentPty);
    }

    private void endWrite(ObjectOutput out) throws IOException {
        synchronized (attributes) {
            out.writeInt(attributes.size());
            for (Iterator<Entry<String, Serializable>> it = attributes.entrySet().iterator(); it.hasNext();) {
                Entry<String, Serializable> k = it.next();
                out.writeUTF(k.getKey());
                out.writeObject(k.getValue());
            }
        }
    }

    private void endRead(ObjectInput in) throws IOException, ClassNotFoundException {
        attributes = Collections.synchronizedMap(new LinkedHashMap<String, Serializable>());
        int s = in.readInt();
        for (int i = 0; i < s; i++) {
            attributes.put(in.readUTF(), (Serializable) in.readObject());
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int currentPty = Thread.currentThread().getPriority();
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        super.readExternal(in);
        int s = in.readInt();
        for (int i = 0; i < s; i++) {
            animsMap.put(in.readUTF(), in.readInt());
        }
        s = in.readInt();
        for (int i = 0; i < s; i++) {
            sfxMap.put(in.readUTF(), in.readInt());
        }
        endRead(in);
        Thread.currentThread().setPriority(currentPty);
    }
}

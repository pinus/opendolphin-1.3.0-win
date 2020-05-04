package open.dolphin.client;

import open.dolphin.infomodel.SchemaModel;

import java.beans.PropertyChangeListener;

/**
 * @author kazm
 */
public interface SchemaEditor {
    public String IMAGE_PROP = "imageProp";

    public void setEditable(boolean b);

    public void setSchema(SchemaModel model);

    public void start();

    public void addPropertyChangeListener(PropertyChangeListener l);

    public void removePropertyChangeListener(PropertyChangeListener l);

}

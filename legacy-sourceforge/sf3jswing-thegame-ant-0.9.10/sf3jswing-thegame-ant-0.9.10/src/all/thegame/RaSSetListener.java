package thegame;


/* <p>Listener pour savoir ce que fait ReadAndSaveSettings </p>
@see ReadAndSaveSettings
*/
public interface RaSSetListener {

    /* Les options ont ete sauvegardees, voila la date */
    void optionsSaved (String date);

    /* Les options ont ete supprimees */
    void optionsDeleted ();

    /* Les options ont ete chargees */
    void optionsLoaded (String date);

    /* Le fichier d'options n'a pas ete trouve, les options ne sont pas chargees */
    void optionsNotFound ();
    
    /***/
    void optionsError(Object e);
    
    /***/
    void optionsAreSaving(String m);
    /***/
    void optionsAreLoading(String m);
}

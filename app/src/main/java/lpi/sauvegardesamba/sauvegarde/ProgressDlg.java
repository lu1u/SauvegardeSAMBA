/**
 *
 */
package lpi.sauvegardesamba.sauvegarde;

/**
 * @author lucien
 */
public interface ProgressDlg
{
void setProgress(String format, int step, int Max);

void setProfil(String profil);

void setPartage(String partage);

void notification(String notification);

boolean isCanceled();
}

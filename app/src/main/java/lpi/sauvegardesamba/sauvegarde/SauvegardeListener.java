/**
 *
 */
package lpi.sauvegardesamba.sauvegarde;

import lpi.sauvegardesamba.profils.Profil;

/**
 * @author lucien
 */
public interface SauvegardeListener
{
void onDepartSauvegarde();

void onFinSauvegarde();

void onProfil(Profil profil);

void onProgress(String format, int step, int Max);
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package FileMngmt;

import java.io.BufferedReader;
import java.io.FileReader;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 *
 * @author stefanopilla
 */
public class FileMenu {
    public FileMenu(String path){
      addRecentFile(String path);
    }


    public void addRecentFile(String path) {
        if (verifyRecentFile()) {
           recentMenuItem.setEnabled(true);
            try {
                FileReader fr = new FileReader(path);
                BufferedReader reader = new BufferedReader(fr);
                String line;
                int i = 0;
                while ((line = reader.readLine()) != null) {
                    final String[] result = line.split(",");
                    for (i = 0; i < result.length; i = i + 2) {
                        freimapgsoc.LayerForm.recentMenuItem = new JMenuItem(result[i]);
                        freimapgsoc.LayerForm.recentMenuItem.setToolTipText(result[i + 1]);
                        freimapgsoc.LayerForm.recentMenuItem.add(freimapgsoc.LayerForm.recentMenuItem, i);
                        path = result[i + 1];
                        freimapgsoc.LayerForm.recentMenuItem.addActionListener(new java.awt.event.ActionListener() {

                            public void actionPerformed(java.awt.event.ActionEvent evt) {
                                freimapgsoc.newRecentFileActionPerformed(evt, path);
                            }
                        });


                    }
                    reader.close();
                    fr.close();

                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    /*
    public void addRecentFile(final String path, String name) {
        try {
            if (verifyRecentFile()) {
                recentFilesMenu.setEnabled(true);
                JMenuItem newRecentFile = new JMenuItem(name);
                recentFilesMenu.add(newRecentFile, recentFilesMenu.getItemCount() - 2);
                newRecentFile.addActionListener(new java.awt.event.ActionListener() {

                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        newRecentFileActionPerformed(evt, path);
                    }
                });
                newRecentFile.setToolTipText(path);
                PrintStream output = new PrintStream(new FileOutputStream(recentFile.getPath()));
                output.append(name + "," + path);
            } else if (!verifyRecentFile()) {
                recentFile.createNewFile();
                recentFilesMenu.setEnabled(true);
                JMenuItem newRecentFile = new JMenuItem(name);
                newRecentFile.addActionListener(new java.awt.event.ActionListener() {

                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        newRecentFileActionPerformed(evt, path);
                    }
                });
                recentFilesMenu.add(newRecentFile, recentFilesMenu.getItemCount() - 2);
                newRecentFile.setToolTipText(path);
                PrintStream output = new PrintStream(new FileOutputStream(recentFile.getPath()));
                output.append(name + "," + path);
            }
        } catch (FileNotFoundException ex) {
            ex.getMessage();
        } catch (IOException io) {
            io.getMessage();
        }
    }*/

    private boolean verifyRecentFile() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private JMenu recentMenu;
}

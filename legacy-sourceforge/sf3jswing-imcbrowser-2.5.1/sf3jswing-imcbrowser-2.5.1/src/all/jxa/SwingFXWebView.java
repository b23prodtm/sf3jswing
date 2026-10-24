package jxa;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * SwingFXWebView
 *
 * @author https://gist.github.com/anjackson/1640654
 */
public class SwingFXWebView extends JPanel {

        private Stage stage;
        private WebView browser;
        private JFXPanel jfxPanel;
        private JButton swingButton;
        private WebEngine webEngine;
        private URL homepage;
        private String stageTitle;

        public SwingFXWebView(URL homepage) {
                super();
                this.homepage = homepage;
                stageTitle = homepage.toString();
                initComponents();
        }

        public static void main(String... args) {
                // Run this later:
                SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                                final JFrame frame = new JFrame();

                                try {
                                        frame.getContentPane().add(new SwingFXWebView(new URL("http://www.java.com")));
                                        frame.setMinimumSize(new Dimension(640, 480));
                                        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                                        frame.setVisible(true);
                                } catch (MalformedURLException ex) {
                                        ex.printStackTrace();
                                }
                        }
                });
        }

        private void initComponents() {

                jfxPanel = new JFXPanel();
                createScene();

                setLayout(new BorderLayout());
                add(jfxPanel, BorderLayout.CENTER);

                swingButton = new JButton();
                swingButton.addActionListener(new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                                Platform.runLater(new Runnable() {

                                        @Override
                                        public void run() {
                                                webEngine.reload();
                                        }
                                });
                        }
                });
                swingButton.setText("Reload");

                add(swingButton, BorderLayout.SOUTH);
        }

        /**
         * createScene
         *
         * Note: Key is that Scene needs to be created and run on "FX user
         * thread" NOT on the AWT-EventQueue Thread
         *
         */
        private void createScene() {
                Platform.runLater(new Runnable() {
                        @Override
                        public void run() {

                                stage = new Stage();

                                stage.setTitle(stageTitle);
                                stage.setResizable(true);

                                Group root = new Group();
                                Scene scene = new Scene(root, 80, 20);
                                stage.setScene(scene);

                                // Set up the embedded browser:
                                browser = new WebView();
                                webEngine = browser.getEngine();
                                webEngine.load(homepage.toString());
                                ObservableList<Node> children = root.getChildren();
                                children.add(browser);

                                jfxPanel.setScene(scene);
                        }
                });
        }

        public WebEngine getWebEngine() {
                return webEngine;
        }

}

package me.giskard.dust.mod.gui.swing;

import java.awt.BorderLayout;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Screen;
import javafx.util.Duration;
import me.giskard.dust.core.Dust;
import me.giskard.dust.core.DustConsts.DustAgent;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class DustGuiSwingVideoPlayerAgent extends DustAgent implements DustGuiSwingConsts {

	private static void initAndShowGUI() {
		JFrame frame = new JFrame("FX");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		final JPanel videoPanel = new JPanel(new BorderLayout());
		frame.getContentPane().add(videoPanel);
		frame.setVisible(true);

		frame.setBounds(100, 100, 800, 400);

		initFX(videoPanel);

	}

	private static void initFX(JPanel videoPanel) {
		final JFXPanel VFXPanel = new JFXPanel();

		File video_source = new File("/Users/lkedves/work/temp/SzE_lecture.mp4");
		Media m = new Media(video_source.toURI().toString());
		MediaPlayer player = new MediaPlayer(m);
		MediaView viewer = new MediaView(player);

		StackPane root = new StackPane();
		Scene scene = new Scene(root);

		// center video position
		javafx.geometry.Rectangle2D screen = Screen.getPrimary().getVisualBounds();
		viewer.setX((screen.getWidth() - videoPanel.getWidth()) / 2);
		viewer.setY((screen.getHeight() - videoPanel.getHeight()) / 2);

		// resize video based on screen size
		DoubleProperty width = viewer.fitWidthProperty();
		DoubleProperty height = viewer.fitHeightProperty();
		width.bind(Bindings.selectDouble(viewer.sceneProperty(), "width"));
		height.bind(Bindings.selectDouble(viewer.sceneProperty(), "height"));
		viewer.setPreserveRatio(true);

		// add video to stackpane
		root.getChildren().add(viewer);
		
	
		VFXPanel.setScene(scene);
		player.play();
		videoPanel.setLayout(new BorderLayout());
		videoPanel.add(VFXPanel, BorderLayout.CENTER);

		Duration d = Duration.minutes(35.0);
	
		player.seek(d);

	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				initAndShowGUI();
			}
		});
	}

	class Player {
		JPanel pnl;
		JFXPanel videoPanel;
		MediaPlayer mp;

		public Player() {
			pnl = new JPanel(new BorderLayout());

			videoPanel = new JFXPanel();

			pnl.add(videoPanel, BorderLayout.CENTER);
		}
	}

	@Override
	protected Object process(DustAccess access) throws Exception {
		String cmd = Dust.access(DustAccess.Peek, "", null, TOKEN_MIND_ATT_CMD);

		DustHandle hRoot = Dust.access(DustAccess.Peek, null, null, TOKEN_MISC_ATT_TARGET);

		switch (cmd) {
		default:
//			sb = new StringBuilder("Unknown command: ").append(cmd);
			break;
		}

		return null;

	}

}

package me.giskard.dust.mod.gui.swing;

import java.awt.BorderLayout;
import java.io.File;
import java.net.URI;

import javax.swing.JPanel;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;
import me.giskard.dust.api.Dust;
import me.giskard.dust.api.DustAgent;
import me.giskard.dust.api.DustHandle;
import me.giskard.handles.giskard_me.DustGenHandles_flow_1;
import me.giskard.handles.giskard_me.DustGenHandles_misc_1;

//@SuppressWarnings({ "unchecked", "rawtypes" })
public class DustGuiSwingVideoPlayerAgent extends DustAgent
		implements DustGuiSwingConsts, DustGenHandles_flow_1, DustGenHandles_misc_1 {

	private static class VideoPanel extends JPanel {
		private static final long serialVersionUID = 1L;

		Media m;
		MediaPlayer player;

		StackPane root;
		Scene scene;
		MediaView viewer;
		JFXPanel vfxPanel;

		public VideoPanel() {
			super(new BorderLayout());

			// must be first for some magic in com.sun.javafx.tk.quantum.QuantumToolkit
			vfxPanel = new JFXPanel();

			viewer = new MediaView();

			// resize video based on screen size
			DoubleProperty width = viewer.fitWidthProperty();
			DoubleProperty height = viewer.fitHeightProperty();
			width.bind(Bindings.selectDouble(viewer.sceneProperty(), "width"));
			height.bind(Bindings.selectDouble(viewer.sceneProperty(), "height"));
			viewer.setPreserveRatio(true);

			root = new StackPane();
			root.getChildren().add(viewer);

			scene = new Scene(root);

			vfxPanel.setScene(scene);

			add(vfxPanel, BorderLayout.CENTER);
		}

		void setMediaUri(URI uri) {
			m = new Media(uri.toString());
			player = new MediaPlayer(m);

			viewer.setMediaPlayer(player);
		}

	}

	@Override
	public void init() throws Exception {

		VideoPanel vp = new VideoPanel();
		test(vp);

		Dust.access(DustAccess.Set, vp, null, HANDLE_DUST_ATT_CTX_AGT, HANDLE_DUST_ATT_WRAPPEDOBJECT);

	}

	@Override
	public void process() throws Exception {
		DustHandle cmd = Dust.access(DustAccess.Peek, null, null, HANDLE_MIND_ATT_CMD);

		VideoPanel vp = Dust.access(DustAccess.Peek, null, null, HANDLE_DUST_ATT_CTX_AGT, HANDLE_DUST_ATT_WRAPPEDOBJECT);
		Duration d = null;

		if ( HANDLE_MISC_TAG_CMD_STOP.equals(cmd) ) {
			vp.player.stop();

		} else if ( HANDLE_FLOW_TAG_CMD_PAUSE.equals(cmd) ) {
			vp.player.pause();

		} else if ( HANDLE_FLOW_TAG_CMD_PLAY.equals(cmd) ) {
			vp.player.play();

		} else if ( HANDLE_MISC_TAG_CMD_SKIPBACK.equals(cmd) ) {
			d = vp.player.getCurrentTime().add(Duration.seconds(-10.0));
		} else if ( HANDLE_MISC_TAG_CMD_SKIPFORWARD.equals(cmd) ) {
			d = vp.player.getCurrentTime().add(Duration.seconds(10.0));
		} else {
			Dust.access(DustAccess.Set, vp, null, HANDLE_DUST_ATT_CTX_MSG, HANDLE_DUST_ATT_WRAPPEDOBJECT);
		}
		
		if ( null != d ) {
			vp.player.seek(d);
		}

	}

	public static void test(VideoPanel vp) {
		File video_source = new File("/Users/lkedves/work/temp/SzE_lecture.mp4");
		URI uri = video_source.toURI();

		vp.setMediaUri(uri);

//		vp.player.play();

		Duration d = Duration.minutes(35.0);

		vp.player.seek(d);
	}

}

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
import me.giskard.dust.core.Dust;
import me.giskard.dust.core.DustConsts.DustAgent;
import me.giskard.tokens.DustGenTokens_flow_1;
import me.giskard.tokens.DustGenTokens_misc_1;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class DustGuiSwingVideoPlayerAgent extends DustAgent implements DustGuiSwingConsts, DustGenTokens_flow_1, DustGenTokens_misc_1 {

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
	protected void init() throws Exception {
		Object ret = Dust.access(DustAccess.Peek, null, DustContext.Agent, TOKEN_DUST_ATT_WRAPPEDOBJECT);

		if (null == ret) {
			VideoPanel vp = new VideoPanel();
			
			test(vp);
			
			ret = vp;
			
			Dust.access(DustAccess.Set, ret, DustContext.Agent, TOKEN_DUST_ATT_WRAPPEDOBJECT);
			Dust.access(DustAccess.Set, ret, DustContext.Service, TOKEN_DUST_ATT_WRAPPEDOBJECT);
		}

	}

	@Override
	protected Object process(DustAccess access) throws Exception {
		String cmd = Dust.access(DustAccess.Peek, null, null, TOKEN_MIND_ATT_CMD, TOKEN_MIND_ATT_ID);

		VideoPanel vp = Dust.access(DustAccess.Peek, null, DustContext.Agent, TOKEN_DUST_ATT_WRAPPEDOBJECT);

		switch (cmd) {
		case TOKEN_MISC_TAG_CMD_STOP:
			vp.player.stop();
			break;
		case TOKEN_FLOW_TAG_CMD_PAUSE:
			vp.player.pause();
			break;
		case TOKEN_FLOW_TAG_CMD_PLAY:
			vp.player.play();
			break;
		case TOKEN_MISC_TAG_CMD_SKIP:
			Duration d = vp.player.getCurrentTime();
			
			d = d.add(Duration.seconds(-10.0));
			vp.player.seek(d);
			break;
		default:
//			sb = new StringBuilder("Unknown command: ").append(cmd);
			Dust.access(DustAccess.Set, vp, DustContext.Service, TOKEN_DUST_ATT_WRAPPEDOBJECT);
			break;
		}

		return null;

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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;

// Play Class
public class GachonCastle implements ActionListener {
	static GachonCastle game;
	static Timer aTimer;
	static MainFrame mainframe;
	static long gameTime;
	
	public static void main(String args[]) {
		game = new GachonCastle();
		
		aTimer = new Timer(1, game);
		
		mainframe = new MainFrame(800, 600, "contents/", 4);
		mainframe.LoadContents(); // 1
		mainframe.Initialization(); // 2
		
		gameTime = 0;
		aTimer.start();
	}

	public void actionPerformed(ActionEvent e)
	{
		mainframe.UpdateGameProc(gameTime++);
	}
}
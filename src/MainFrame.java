import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Hashtable;

import javax.swing.*;

@SuppressWarnings("serial")
public class MainFrame extends JFrame implements KeyListener, MouseListener, MouseMotionListener {

	// public
	public int WINDOW_WIDTH;
	public int WINDOW_HEIGHT;
	public String contentPath;
	public enum GameStatus {
		Playing, Finished, Started
	}

	public GameStatus gamestatus;
	// private
	private int blockkindsCount;
	private int initial_blockcount;
	private Block[][] blocklist;

	// 배경 타일을 저장. 이미지를 중복해서 타일형태로 그리도록 paint()에서 구현되어있음.
	private GameImage img_background;
	// 클리어 화면을 저장.
	private GameImage img_success;
	// 시작 화면을 저장
	private GameImage img_start;
	private int start_status = 0;


	private Hashtable<String, GameSound> gamesound_table;
	private String[] gamesoundlist = { "click", "win", "ding" };

	private int mousex;
	private int mousey;
	private boolean mouseclicked;

	private Graphics backbuf;
	private Image backbuf_image;

	private int level = 1;
	private JLabel label;

	// set up the board
	char[][] boardtable = { { 'x', 'o', 'o', 'o', 'x' }, { 'o', 'o', 'o', 'o', 'o' },
			{ 'o', 'o', 'o', 'o', 'o' }, { 'o', 'o', 'o', 'o', 'o' }, { 'x', 'x', 'o', 'o', 'x' } };// x:nil,

	// o:block

	// initializing constants for the game frame
	public MainFrame(int _width, int _height, String contentPath, int blockkindsCount) {
		if (_width < 0 || _height < 0)
			throw new RuntimeException("we need realistic size of a window");
		WINDOW_WIDTH = _width;
		WINDOW_HEIGHT = _height;
		this.contentPath = contentPath;
		this.blockkindsCount = blockkindsCount;
		gamesound_table = new Hashtable<String, GameSound>();
		blocklist = null;

		gamestatus = GameStatus.Playing;
	}


	// execute it second
	// setting basic variables for the game environment
	public void Initialization() {
		this.Initialization(0);
		this.Initialization(1);
	}

	public void Initialization(int which) {
		switch (which) {
			case 0: // setting window environments and operational functions, so initialize it before case0
				setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
				setDefaultCloseOperation(EXIT_ON_CLOSE);
				setTitle("Gachon Cattle (가천성) - The Game");
				setResizable(false);
				setVisible(true);
				addKeyListener(this);
				addMouseListener(this);
				addMouseMotionListener(this);
				break;
			case 1: // set up randomized block kinds
				int[] blockKindsTable = new int[initial_blockcount];
				for (int i = 0; i < blockKindsTable.length; i++)
					blockKindsTable[i] = (i / 2) % blockkindsCount;

				for (int i = 0; i < blockKindsTable.length; i++) {
					int tmp = blockKindsTable[i];
					int ri = i + (int) (Math.random() * (blockKindsTable.length - i));
					blockKindsTable[i] = blockKindsTable[ri];
					blockKindsTable[ri] = tmp;
				}

				// initialize block
				for (int i = 0; i < blocklist.length; i++)
					for (int j = 0; j < blocklist[i].length; j++) {
						if (boardtable[i][j] == 'x')
							blocklist[i][j].blockstate = Block.BlockState.dead;
						else
							blocklist[i][j].blockstate = Block.BlockState.alive;
					}

				int k = 0;
				for (int i = 0; i < blocklist.length; i++)
					for (int j = 0; j < blocklist[i].length; j++)
						if (blocklist[i][j].blockstate != Block.BlockState.dead)
							blocklist[i][j].BlockKinds = blockKindsTable[k++];
				// reset game state
				this.gamestatus = GameStatus.Playing;
				break;
		}
	}

	// execute it first
	// load images and sounds for the game
	public void LoadContents() {
		if (gamesound_table == null)
			throw new RuntimeException("something's wrong during LoadContents");

		gamesound_table.clear();
		// load background
		img_background = new GameImage();
		img_background.LoadImage(contentPath + "background" + ".png", 1, 0);
		// load success img
		img_success = new GameImage();
		img_success.LoadImage(contentPath + "success" + ".png", 1, 0);
		// load start img
		img_start = new GameImage();
		img_start.LoadImage(contentPath + "start" + ".png", 1, 0);


		// load contents for block
		// create blocks (using boardtable)
		int startposx = Math.max(0, WINDOW_WIDTH - boardtable.length * 50) / 2;
		int startposy = Math.max(0, WINDOW_HEIGHT - boardtable[0].length * 60) / 2;
		blocklist = new Block[boardtable.length][boardtable[0].length];

		for (int i = 0; i < blocklist.length; i++)
			for (int j = 0; j < blocklist[i].length; j++) {
				/*
				 * Block의 생성은 Initialization에서 하는 것이 맞지만,
				 * 코드의 간략화를 위해 Block의 생성자에 init을 포함하였으므로 여기서 생성.
				 * 마지막 인자인 block의 종류를 0으로 고정함으로서 이후 initialization()에서
				 * 할당하도록 구현함.
				 */
				blocklist[i][j] = new Block(i * (50 - 5) + startposx, j * (60 - 5) + startposy, 50,
						60, 0, 0, 5, 5, 0);
				blocklist[i][j].LoadContents(contentPath);

				if (boardtable[i][j] == 'x')
					blocklist[i][j].blockstate = Block.BlockState.dead;
			}

		// check block table. The number of 'o' must be even number
		initial_blockcount = 0;
		for (char[] chlist : boardtable)
			for (char ch : chlist)
				if (ch == 'o')
					initial_blockcount++;
		if ((initial_blockcount & 1) == 1 || initial_blockcount == 0)
			throw new RuntimeException("input continas non-even # of blocks");

		// start loading (game sound)
		GameSound sndloaded = null;
		for (String loadinglistitem : gamesoundlist) {
			sndloaded = new GameSound(contentPath + loadinglistitem + ".wav");
			gamesound_table.put(loadinglistitem, sndloaded);
		}
	}

	// called by JAVA automatically
	public void update()
	{
		repaint();
	}

	//main logic of the game
	public void UpdateGameProc(long gameTime) {

		switch (this.gamestatus) {

			case Started:
				if (mouseclicked) {
					this.Initialization(1);
					mouseclicked = false;
				}
				break;

			case Playing:
				if (start_status == 0) {
					gamestatus = GameStatus.Started;
					start_status = 1;

					break;
				}

				// 각 Block의 mouse event 처리를 위한 하위방향의 call
				for (Block[] bllist : blocklist)
					for (Block bl : bllist)
						bl.UpdateGameProc(mousex, mousey, mouseclicked);
				mouseclicked = false;

				// 블럭 삭제 성공 여부를 체크
				// 우선 현재 선택된 블럭이 2개인지를 확인.
				if (Block.selectedlist.size() == 2) {
					if (Block.selectedlist.getFirst() == Block.selectedlist.getLast()) {
						// 같은 블럭 두번 클릭 시 -> deselect
						Block.selectedlist.getFirst().blockstate = Block.BlockState.alive;
						Block.selectedlist.getLast().blockstate = Block.BlockState.alive;

						gamesound_table.get("ding").Play();
					} else if (checkBlockCol(Block.selectedlist.getFirst(), Block.selectedlist
							.getLast())) {
						// 두 블럭이 삭제 가능할 시 -> remove
						Block.selectedlist.getFirst().blockstate = Block.BlockState.dead;
						Block.selectedlist.getLast().blockstate = Block.BlockState.dead;

						gamesound_table.get("click").Play();
					}

					else {
						// 그 외의 사항 -> deselect
						Block.selectedlist.getFirst().blockstate = Block.BlockState.alive;
						Block.selectedlist.getLast().blockstate = Block.BlockState.alive;

						gamesound_table.get("ding").Play();
					}
					Block.selectedlist.clear();
				}

				// check whether the game is finished
				boolean isgamefinished = true;
				for (Block[] bllist : blocklist)
					for (Block bl : bllist)
						isgamefinished &= (bl.blockstate == Block.BlockState.dead);
				if (isgamefinished) {
					gamestatus = GameStatus.Finished;
					gamesound_table.get("win").Play();
				}
				break;

			case Finished:
				if (mouseclicked) {
					this.Initialization(1);
					mouseclicked = false;

					level++;
				}
				break;
		}

		// 이를 호출해야 Java에서 Game화면을 다시 그림
		repaint();
	}

	// checks the block's state in (i, j)
	private final boolean isBlockAlreadyExistAt(int i, int j) {
		if (i < 0 || j < 0 || i >= blocklist.length || j >= blocklist[i].length)
			return false;
		return blocklist[i][j].blockstate != Block.BlockState.dead;
	}

	// check for linear cases
	// return true when there are no obstacles between two blocks
	private final boolean checkBlockColSub1(int fi, int fj, int li, int lj) {
		if (fi == li) {
			for (int j = Math.min(fj, lj) + 1; j < Math.max(fj, lj); j++)
				if (isBlockAlreadyExistAt(fi, j))
					return false;
		}

		else if (fj == lj) {
			for (int i = Math.min(fi, li) + 1; i < Math.max(fi, li); i++)
				if (isBlockAlreadyExistAt(i, fj))
					return false;

		} else
			return false;
		return true;
	}

	// check for 한번 꺾인 선 - 'ㄱ' 모양
	// 두 블럭에서 직선으로 연결된 선들이 만나는 점을 기준으로, 각 점들과 직선관계가 성립되는지 확인
	private final boolean checkBlockColSub2(int fi, int fj, int li, int lj) {
		if (!isBlockAlreadyExistAt(fi, lj)) {
			if (checkBlockColSub1(fi, fj, fi, lj) && checkBlockColSub1(fi, lj, li, lj))
				return true;
		}

		if (!isBlockAlreadyExistAt(li, fj)) {
			if (checkBlockColSub1(li, fj, fi, fj) && checkBlockColSub1(li, fj, li, lj))
				return true;
		}
		return false;
	}

	// check for '두번 꺾인 선 - 'ㄷ'모양
	// checks adjacent block which have 'ㄱ' or 'ㄴ' relation with other block
	private final boolean checkBlockColSub3(int fi, int fj, int li, int lj) {
		boolean check = false;
		if (!isBlockAlreadyExistAt(fi + 1, fj))
			check |= checkBlockColSub2(fi + 1, fj, li, lj);
		if (!isBlockAlreadyExistAt(fi - 1, fj))
			check |= checkBlockColSub2(fi - 1, fj, li, lj);
		if (!isBlockAlreadyExistAt(fi, fj + 1))
			check |= checkBlockColSub2(fi, fj + 1, li, lj);
		if (!isBlockAlreadyExistAt(fi, fj - 1))
			check |= checkBlockColSub2(fi, fj - 1, li, lj);

		if (!isBlockAlreadyExistAt(li + 1, lj))
			check |= checkBlockColSub2(li + 1, lj, fi, fj);
		if (!isBlockAlreadyExistAt(li - 1, lj))
			check |= checkBlockColSub2(li - 1, lj, fi, fj);
		if (!isBlockAlreadyExistAt(li, lj + 1))
			check |= checkBlockColSub2(li, lj + 1, fi, fj);
		if (!isBlockAlreadyExistAt(li, lj - 1))
			check |= checkBlockColSub2(li, lj - 1, fi, fj);
		return check;
	}

	// checks every case here using upper sub functions, if two blocks are deletable return true
	private boolean checkBlockCol(Block f, Block l) {
		boolean check1 = false;
		boolean check2 = false;
		boolean check3 = false;
		// check whether two blocks are same kind. Only deletable when two selectable blocks are same kind.
		if (f.BlockKinds != l.BlockKinds)
			return false;
		// get location of two blocks in two-dimensional array way
		int fi = 0, fj = 0;
		int li = 0, lj = 0;
		int cnt = 0;

		//initializing location
		for (int i = 0; i < blocklist.length; i++) {
			for (int j = 0; j < blocklist[i].length; j++) {
				if (cnt < 2) {
					if (blocklist[i][j] == f) {
						fi = i;
						fj = j;
					}

					else if (blocklist[i][j] == l) {
						li = i;
						lj = j;
					}
				}
			}
			if (cnt == 2)
				break;
		}
		// check1
		if (true)
			check1 = checkBlockColSub1(fi, fj, li, lj);
		// check2 (한번 꺾인 선 - 'ㄱ')
		if (!check1) // 이 외의 경우엔 검사할 필요 없음.
			check2 = checkBlockColSub2(fi, fj, li, lj);
		// check3 (두번 꺾인 선 - 'ㄷ'모양)
		if (!check1 && !check2) // 이 외의 경우엔 검사할 필요 없음.
			check3 = checkBlockColSub3(fi, fj, li, lj);

		if (check1)
			debug.print("we can take check1");
		if (check2)
			debug.print("we can take check2");
		if (check3)
			debug.print("we can take check3");

		return (check1 || check2 || check3);
	}


	//function to paint the frame
	public void paint(Graphics g) {
		// 깜박거림을 방지하기 위해 backbuffer를 생성하여 Double Buffering을 구현.
		Dimension dim = this.getSize();
		if (backbuf == null) {
			backbuf_image = createImage(dim.width, dim.height);
			backbuf = backbuf_image.getGraphics();
		}

		backbuf.setColor(Color.white);
		backbuf.fillRect(0, 0, dim.width, dim.height);

		// draw background
		for (int wi = 0; wi < dim.width; wi += img_background.getWidth())
			for (int wj = 0; wj < dim.height; wj += img_background.getHeight())
				backbuf.drawImage(img_background.getImage(), wi, wj, this);

		for (int i = blocklist.length - 1; i >= 0; i--)
			for (int j = 0; j < blocklist[i].length; j++)
				blocklist[i][j].paint(backbuf, this);

		// 시작 화면
		if (this.gamestatus == GameStatus.Started)
			backbuf.drawImage(img_start.getImage(), (WINDOW_WIDTH - img_start.getWidth()) / 2,
					(WINDOW_HEIGHT - img_start.getHeight()) / 2, this);

		// 성공 화면
		if (this.gamestatus == GameStatus.Finished)
			backbuf.drawImage(img_success.getImage(), (WINDOW_WIDTH - img_success.getWidth()) / 2,
					(WINDOW_HEIGHT - img_success.getHeight()) / 2, this);

		// Drawing on FrontSurface (backbuffer의 그림을 frontbuffer로 복사)
		g.drawImage(backbuf_image, 0, 0, null);
	}

	// ****************************************


	// if x is pressed, terminate the game
	@Override
	public void keyPressed(KeyEvent arg0) {
		char keychar = arg0.getKeyChar();
		switch (keychar) {
			case 'x':
			case 'X':
				System.exit(1);
				break;
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		mousex = arg0.getX();
		mousey = arg0.getY();
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		if ((arg0.getModifiers() & MouseEvent.BUTTON1_MASK) != 0)
			mouseclicked = true;
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		mousex = arg0.getX();
		mousey = arg0.getY();
	}
}
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
	// methods
	/**
	 * 게임프레임의 생성자.
	 * 게임의 상수 등을 설정한다.
	 */
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

	// start it second
	/**
	 * 이 함수를 두번재로 실행.
	 * 게임의 기본 환경값 (window, 변수)을 설정한다.
	 */
	public void Initialization() {
		this.Initialization(0);
		this.Initialization(1);
	}

	/**
	 * @param which
	 *            : App를 끄지 않아도 block의 재생성이 가능하도록 나눔. '1'번인자가 그것.
	 *            단, window 설정 등이 모두 '0'번에 몰려있으므로, App실행 시 맨 처음에는 '0'을 실행해야 한다.
	 *            (대신 Initialization(void)를 호출할 수도 있다.)
	 */
	public void Initialization(int which) {

		switch (which) {
		case 0:
			setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
			setDefaultCloseOperation(EXIT_ON_CLOSE);
			setTitle("Gachon Cattle (가천성) - The Game");
			setResizable(false);
			setVisible(true);
			addKeyListener(this);
			addMouseListener(this);
			addMouseMotionListener(this);
			break;
		case 1:
			// set up randomized block kinds
			int[] blockKindsTable = new int[initial_blockcount];
			for (int i = 0; i < blockKindsTable.length; i++)
				blockKindsTable[i] = (i / 2) % blockkindsCount;

			for (int i = 0; i < blockKindsTable.length; i++) {
				int tmp = blockKindsTable[i];
				int ri = i + (int) (Math.random() * (blockKindsTable.length - i));
				blockKindsTable[i] = blockKindsTable[ri];
				blockKindsTable[ri] = tmp;
			}

			// init block
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

	/**`
	 * 이 함수를 첫번째로 실행.
	 * 게임에 필요한 그림, 소리 등을 불러온다.
	 */
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
		// load success img
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
				/**
				 * block margin 추가 및 그에 따른 position 값 정리.
				 * (margin = 0,0,5,5 == top,right,bottom,left)
				 * Mouse클릭 시 블럭에서 margin을 제외한 부분만을 인식.
				 * 
				 *  그리는 위치는 (기존x-margin_l+r, 기존y-margin_t+b)로 되어있는데
				 *  본래 margin을 계산해서 정해야 하지만 대신에 코드의 심플함을 선택함. 
				 */
				blocklist[i][j] = new Block(i * (50 - 5) + startposx, j * (60 - 5) + startposy, 50,
						60, 0, 0, 5, 5, 0);
				blocklist[i][j].LoadContents(contentPath);

				if (boardtable[i][j] == 'x')
					blocklist[i][j].blockstate = Block.BlockState.dead;
			}

		// block table을 체크. 'o'의 숫자(블럭)은 짝수여야 함.
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

	/**
	 * called by JAVA (automatically)
	 */
	public void update()
	{
		repaint();
	}

	/**
	 * 게임의 주요 로직을 구현
	 * 
	 * @param gameTime
	 *            : currently not used.
	 */
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

			// 게임 종료 체크.
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

		// 이를 호출해야 Java에서 Game화면을 다시 그린다.
		repaint();
	}

	/**
	 * 해당 칸에 블럭이 존재하는지 여부를 리턴.
	 * 
	 * @param i
	 * @param j
	 * @return True: 블럭이 존재, False: 블럭이 존재하지 않거나 배열 범위를 벗어남.
	 */
	private final boolean isBlockAlreadyExistAt(int i, int j) {
		if (i < 0 || j < 0 || i >= blocklist.length || j >= blocklist[i].length)
			return false;
		return blocklist[i][j].blockstate != Block.BlockState.dead;
	}
	/**
	 * 직선상에 있는 두 블럭의 체크를 위함.
	 * 
	 * @param fi
	 * @param fj
	 * @param li
	 * @param lj
	 * @return True: 삭제 가능.
	 */

	private final boolean checkBlockColSub1(int fi, int fj, int li, int lj) {
		// check1 (직선 - 'ㅡ')
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

	/**
	 * 'ㄱ'자 모양으로 위치한 블럭 삭제를 체크.
	 * 내부적으로 checkBlockColSub1을 사용.
	 * 
	 * @param fi
	 * @param fj
	 * @param li
	 * @param lj
	 * @return
	 */
	private final boolean checkBlockColSub2(int fi, int fj, int li, int lj) {
		if (!isBlockAlreadyExistAt(fi, lj)) {
			if (checkBlockColSub1(fi, fj, fi, lj) && checkBlockColSub1(fi, lj, li, lj))
				return true;
		}

		/**
		 * else if (!isBlockAlreadyExistAt(li, fj)) ---> if (!isBlockAlreadyExistAt(li, fj))
		 */
		if (!isBlockAlreadyExistAt(li, fj)) {
			if (checkBlockColSub1(li, fj, fi, fj) && checkBlockColSub1(li, fj, li, lj))
				return true;
		}
		return false;
	}

	/**
	 * 'ㄷ'자 모양으로 위치한 블럭 삭제를 체크.
	 * 규칙 상
	 * x o 1
	 * 1 o x : x는 empty, o는 장애물, 1은 체크할 두 블럭 > 이 상황은 가능,
	 * x o x
	 * 1 o 1 > 이 상황은 불가능.
	 * 내부적으로 checkBlockColSub2를 사용
	 * 
	 * @param fi
	 * @param fj
	 * @param li
	 * @param lj
	 * @return
	 */
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

	/**
	 * 실제로 체크를 위해 호출할 함수. 블럭 두개를 인자로 받아
	 * 내부적으로 배열 위치로 변형하여 사용한다.
	 * 
	 * @param f
	 * @param l
	 * @return
	 */
	private boolean checkBlockCol(Block f, Block l) {
		boolean check1 = false;
		boolean check2 = false;
		boolean check3 = false;
		// 같은 형태의 블럭인지 확인
		if (f.BlockKinds != l.BlockKinds)
			return false;
		// getpos -> do better by reverse-calculation of block-position
		int fi = 0, fj = 0;
		int li = 0, lj = 0;
		int cnt = 0;

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

	/**
	 * Java에 의해서 호출되는 화면 그리는 함수.
	 */
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
		// draw blocks (backbuffer를 인자로 전달. Block의 그림은 Block이 그리도록 함)
		/*
		 * for (Block[] bllist : blocklist)
		 * for (Block bl : bllist)
		 * bl.paint(backbuf, this);
		 */

		/**
		 * 블럭 그림안에 그림자가 좌측 하단방향으로 생기게 되어있기 때문에
		 * 우측 상단의 블럭들 부터 그리게 하여 그림자가 블럭을 덮어쓰지 않게 함. 
		 */
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
	// ****************************************
	// ****************************************
	/**
	 * x키를 누르면 프로그램 종료
	 */
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

	/**
	 * 마우스 클릭 시 변수 값 갱신.
	 */
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

	/**
	 * 마우스 클릭 시 변수 값 갱신.
	 */
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

	/**
	 * 마우스 이동 시 변수 값 갱신.
	 */
	@Override
	public void mouseMoved(MouseEvent arg0) {
		mousex = arg0.getX();
		mousey = arg0.getY();
	}
}
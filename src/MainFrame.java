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

	// ��� Ÿ���� ����. �̹����� �ߺ��ؼ� Ÿ�����·� �׸����� paint()���� �����Ǿ�����.
	private GameImage img_background;
	// Ŭ���� ȭ���� ����.
	private GameImage img_success;
	// ���� ȭ���� ����
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
	 * ������������ ������.
	 * ������ ��� ���� �����Ѵ�.
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
	 * �� �Լ��� �ι���� ����.
	 * ������ �⺻ ȯ�氪 (window, ����)�� �����Ѵ�.
	 */
	public void Initialization() {
		this.Initialization(0);
		this.Initialization(1);
	}

	/**
	 * @param which
	 *            : App�� ���� �ʾƵ� block�� ������� �����ϵ��� ����. '1'�����ڰ� �װ�.
	 *            ��, window ���� ���� ��� '0'���� ���������Ƿ�, App���� �� �� ó������ '0'�� �����ؾ� �Ѵ�.
	 *            (��� Initialization(void)�� ȣ���� ���� �ִ�.)
	 */
	public void Initialization(int which) {

		switch (which) {
		case 0:
			setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
			setDefaultCloseOperation(EXIT_ON_CLOSE);
			setTitle("Gachon Cattle (��õ��) - The Game");
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
	 * �� �Լ��� ù��°�� ����.
	 * ���ӿ� �ʿ��� �׸�, �Ҹ� ���� �ҷ��´�.
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
				 * Block�� ������ Initialization���� �ϴ� ���� ������,
				 * �ڵ��� ����ȭ�� ���� Block�� �����ڿ� init�� �����Ͽ����Ƿ� ���⼭ ����.
				 * ������ ������ block�� ������ 0���� ���������μ� ���� initialization()����
				 * �Ҵ��ϵ��� ������.
				 */
				/**
				 * block margin �߰� �� �׿� ���� position �� ����.
				 * (margin = 0,0,5,5 == top,right,bottom,left)
				 * MouseŬ�� �� ������ margin�� ������ �κи��� �ν�.
				 * 
				 *  �׸��� ��ġ�� (����x-margin_l+r, ����y-margin_t+b)�� �Ǿ��ִµ�
				 *  ���� margin�� ����ؼ� ���ؾ� ������ ��ſ� �ڵ��� �������� ������. 
				 */
				blocklist[i][j] = new Block(i * (50 - 5) + startposx, j * (60 - 5) + startposy, 50,
						60, 0, 0, 5, 5, 0);
				blocklist[i][j].LoadContents(contentPath);

				if (boardtable[i][j] == 'x')
					blocklist[i][j].blockstate = Block.BlockState.dead;
			}

		// block table�� üũ. 'o'�� ����(��)�� ¦������ ��.
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
	 * ������ �ֿ� ������ ����
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

			// �� Block�� mouse event ó���� ���� ���������� call
			for (Block[] bllist : blocklist)
				for (Block bl : bllist)
					bl.UpdateGameProc(mousex, mousey, mouseclicked);
			mouseclicked = false;

			// �� ���� ���� ���θ� üũ
			// �켱 ���� ���õ� ���� 2�������� Ȯ��.
			if (Block.selectedlist.size() == 2) {
				if (Block.selectedlist.getFirst() == Block.selectedlist.getLast()) {
					// ���� �� �ι� Ŭ�� �� -> deselect
					Block.selectedlist.getFirst().blockstate = Block.BlockState.alive;
					Block.selectedlist.getLast().blockstate = Block.BlockState.alive;

					gamesound_table.get("ding").Play();
				} else if (checkBlockCol(Block.selectedlist.getFirst(), Block.selectedlist
						.getLast())) {
					// �� ���� ���� ������ �� -> remove
					Block.selectedlist.getFirst().blockstate = Block.BlockState.dead;
					Block.selectedlist.getLast().blockstate = Block.BlockState.dead;

					gamesound_table.get("click").Play();
				}

				else {
					// �� ���� ���� -> deselect
					Block.selectedlist.getFirst().blockstate = Block.BlockState.alive;
					Block.selectedlist.getLast().blockstate = Block.BlockState.alive;

					gamesound_table.get("ding").Play();
				}
				Block.selectedlist.clear();
			}

			// ���� ���� üũ.
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

		// �̸� ȣ���ؾ� Java���� Gameȭ���� �ٽ� �׸���.
		repaint();
	}

	/**
	 * �ش� ĭ�� ���� �����ϴ��� ���θ� ����.
	 * 
	 * @param i
	 * @param j
	 * @return True: ���� ����, False: ���� �������� �ʰų� �迭 ������ ���.
	 */
	private final boolean isBlockAlreadyExistAt(int i, int j) {
		if (i < 0 || j < 0 || i >= blocklist.length || j >= blocklist[i].length)
			return false;
		return blocklist[i][j].blockstate != Block.BlockState.dead;
	}
	/**
	 * ������ �ִ� �� ���� üũ�� ����.
	 * 
	 * @param fi
	 * @param fj
	 * @param li
	 * @param lj
	 * @return True: ���� ����.
	 */

	private final boolean checkBlockColSub1(int fi, int fj, int li, int lj) {
		// check1 (���� - '��')
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
	 * '��'�� ������� ��ġ�� �� ������ üũ.
	 * ���������� checkBlockColSub1�� ���.
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
	 * '��'�� ������� ��ġ�� �� ������ üũ.
	 * ��Ģ ��
	 * x o 1
	 * 1 o x : x�� empty, o�� ��ֹ�, 1�� üũ�� �� �� > �� ��Ȳ�� ����,
	 * x o x
	 * 1 o 1 > �� ��Ȳ�� �Ұ���.
	 * ���������� checkBlockColSub2�� ���
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
	 * ������ üũ�� ���� ȣ���� �Լ�. �� �ΰ��� ���ڷ� �޾�
	 * ���������� �迭 ��ġ�� �����Ͽ� ����Ѵ�.
	 * 
	 * @param f
	 * @param l
	 * @return
	 */
	private boolean checkBlockCol(Block f, Block l) {
		boolean check1 = false;
		boolean check2 = false;
		boolean check3 = false;
		// ���� ������ ������ Ȯ��
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
		// check2 (�ѹ� ���� �� - '��')
		if (!check1) // �� ���� ��쿣 �˻��� �ʿ� ����.
			check2 = checkBlockColSub2(fi, fj, li, lj);
		// check3 (�ι� ���� �� - '��'���)
		if (!check1 && !check2) // �� ���� ��쿣 �˻��� �ʿ� ����.
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
	 * Java�� ���ؼ� ȣ��Ǵ� ȭ�� �׸��� �Լ�.
	 */
	public void paint(Graphics g) {
		// ���ڰŸ��� �����ϱ� ���� backbuffer�� �����Ͽ� Double Buffering�� ����.
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
		// draw blocks (backbuffer�� ���ڷ� ����. Block�� �׸��� Block�� �׸����� ��)
		/*
		 * for (Block[] bllist : blocklist)
		 * for (Block bl : bllist)
		 * bl.paint(backbuf, this);
		 */

		/**
		 * �� �׸��ȿ� �׸��ڰ� ���� �ϴܹ������� ����� �Ǿ��ֱ� ������
		 * ���� ����� ���� ���� �׸��� �Ͽ� �׸��ڰ� ���� ����� �ʰ� ��. 
		 */
		for (int i = blocklist.length - 1; i >= 0; i--)
			for (int j = 0; j < blocklist[i].length; j++)
				blocklist[i][j].paint(backbuf, this);

		// ���� ȭ��
		if (this.gamestatus == GameStatus.Started)
			backbuf.drawImage(img_start.getImage(), (WINDOW_WIDTH - img_start.getWidth()) / 2,
					(WINDOW_HEIGHT - img_start.getHeight()) / 2, this);

		// ���� ȭ��
		if (this.gamestatus == GameStatus.Finished)
			backbuf.drawImage(img_success.getImage(), (WINDOW_WIDTH - img_success.getWidth()) / 2,
					(WINDOW_HEIGHT - img_success.getHeight()) / 2, this);

		// Drawing on FrontSurface (backbuffer�� �׸��� frontbuffer�� ����)
		g.drawImage(backbuf_image, 0, 0, null);
	}

	// ****************************************
	// ****************************************
	// ****************************************
	/**
	 * xŰ�� ������ ���α׷� ����
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
	 * ���콺 Ŭ�� �� ���� �� ����.
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
	 * ���콺 Ŭ�� �� ���� �� ����.
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
	 * ���콺 �̵� �� ���� �� ����.
	 */
	@Override
	public void mouseMoved(MouseEvent arg0) {
		mousex = arg0.getX();
		mousey = arg0.getY();
	}
}
package heyoom.first.board.repository;

import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;
import org.springframework.jdbc.core.RowMapper;

import org.springframework.jdbc.core.JdbcTemplate;

import heyoom.first.board.domain.Board;
import heyoom.first.board.domain.BoardStatus;
import heyoom.first.security.Seed;

public class BoardRepositoryImpl implements BoardRepository {
	
	private final JdbcTemplate jdbcTemplate;
	
	public BoardRepositoryImpl(DataSource dataSource) {
		jdbcTemplate = new JdbcTemplate(dataSource);
	}

	
	@Override
	public List<Board> getQuestions() {
		return jdbcTemplate.query("SELECT a.bbd_seq, a.ans_seq, a.inq_security_yn, a.bbd_title, \r\n"
				+ "             (SELECT MAX(ans_seq) FROM bbd.t_bbd WHERE bbd_seq = a.bbd_seq) AS answer_count,\r\n"
				+ "             a.reg_writer, a.reg_datetime, a.bbd_password,\r\n"
				+ "             IFNULL((SELECT SUM(day_views) FROM bbd.t_inq_cnt WHERE bbd_seq = a.bbd_seq and ans_seq =a.ans_seq), 0) AS total_views \r\n"
				+ "       FROM bbd.t_bbd a WHERE a.ans_seq = 0 \r\n"
				+ "       ORDER BY a.bbd_seq desc", questionListMapper());
	}
	
	@Override
	public List<Board> getAnswers(Long id) {
		return jdbcTemplate.query("SELECT a.bbd_seq, a.ans_seq, a.inq_security_yn, a.bbd_title, \r\n"
				+ "             a.reg_writer, a.reg_datetime, a.bbd_password,\r\n"
				+ "             IFNULL((SELECT SUM(day_views) FROM bbd.t_inq_cnt WHERE bbd_seq = a.bbd_seq and ans_seq =a.ans_seq), 0) AS total_views \r\n"
				+ "       FROM bbd.t_bbd a WHERE a.bbd_seq=? AND a.ans_seq > 0 \r\n"
				+ "       ORDER BY a.ans_seq asc", answerListMapper(), id);
	}

	@Override
	public int getTotalBoards() {
		Integer count = jdbcTemplate.queryForObject("select count(*) from t_bbd", Integer.class);
		return count;
	}
	
	@Override
	public List<Board> getBoardsOfTitle(String searchWord) {
		String sql = "SELECT a.bbd_seq, a.ans_seq, a.inq_security_yn, a.bbd_title, \r\n"
				+ "(SELECT MAX(ans_seq) FROM bbd.t_bbd WHERE bbd_seq = a.bbd_seq) AS answer_count,\r\n"
				+ "a.reg_writer, a.reg_datetime, a.bbd_password,\r\n"
				+ "(SELECT sum(day_views) from bbd.t_inq_cnt where bbd_seq = a.bbd_seq and ans_seq =a.ans_seq) AS total_views\r\n"
				+ "FROM bbd.t_bbd a  WHERE a.bbd_title LIKE '%" + searchWord + "%'\r\n"
				+ "ORDER BY a.bbd_seq DESC, a.ans_seq ASC";
		return jdbcTemplate.query(sql, boardListMapper());
	}


	@Override
	public List<Board> getBoardsOfWriter(String searchWord) {
		String sql = "SELECT a.bbd_seq, a.ans_seq, a.inq_security_yn, a.bbd_title, \r\n"
				+ "(SELECT MAX(ans_seq) FROM bbd.t_bbd WHERE bbd_seq = a.bbd_seq) AS answer_count,\r\n"
				+ "a.reg_writer, a.reg_datetime, a.bbd_password,\r\n"
				+ "(SELECT sum(day_views) from bbd.t_inq_cnt where bbd_seq = a.bbd_seq and ans_seq =a.ans_seq) AS total_views\r\n"
				+ "FROM bbd.t_bbd a  WHERE a.reg_writer LIKE '%" + searchWord + "%'\r\n"
				+ "ORDER BY a.bbd_seq DESC, a.ans_seq ASC";
		return jdbcTemplate.query(sql, boardListMapper());
	}

	@Override
	public Optional<Board> getBoard(Long bbdId, Long ansId) {
		List<Board> result = jdbcTemplate.query("SELECT a.bbd_seq, a.ans_seq, a.inq_security_yn, a.bbd_title, "
				+ "(SELECT MAX(ans_seq) FROM bbd.t_bbd WHERE bbd_seq = a.bbd_seq) AS answer_count, "
				+ "a.reg_writer, a.reg_datetime, a.bbd_content, a.bbd_attach_1, a.bbd_password, "
				+ "IFNULL((SELECT SUM(day_views) FROM bbd.t_inq_cnt WHERE bbd_seq = a.bbd_seq and ans_seq =a.ans_seq), 0) AS total_views "
				+ "FROM bbd.t_bbd a WHERE a.bbd_seq=? AND a.ans_seq=?", boardMapper(), bbdId, ansId);
		return result.stream().findAny();
	}
	
	@Override
	public List<BoardStatus> getChart() {
		String sql = "      SELECT ?, SUM(a.day_views), SUM(b.day_writes) \r\n"
				+ "          FROM t_inq_cnt a, t_ans_cnt b\r\n"
				+ "         WHERE a.inq_date = b.write_date\r\n"
				+ "           AND a.inq_date = ?\r\n"
				+ "       union\r\n"
				+ "        SELECT ?, SUM(a.day_views), SUM(b.day_writes) \r\n"
				+ "          FROM t_inq_cnt a, t_ans_cnt b\r\n"
				+ "         WHERE a.inq_date = b.write_date\r\n"
				+ "           AND a.inq_date = ?\r\n"
				+ "       union\r\n"
				+ "        SELECT ?, SUM(a.day_views), SUM(b.day_writes) \r\n"
				+ "          FROM t_inq_cnt a, t_ans_cnt b\r\n"
				+ "         WHERE a.inq_date = b.write_date\r\n"
				+ "           AND a.inq_date = ?\r\n"
				+ "       union\r\n"
				+ "        SELECT ?, SUM(a.day_views), SUM(b.day_writes) \r\n"
				+ "          FROM t_inq_cnt a, t_ans_cnt b\r\n"
				+ "         WHERE a.inq_date = b.write_date\r\n"
				+ "           AND a.inq_date = ?\r\n"
				+ "       union\r\n"
				+ "        SELECT ?, SUM(a.day_views), SUM(b.day_writes) \r\n"
				+ "          FROM t_inq_cnt a, t_ans_cnt b\r\n"
				+ "         WHERE a.inq_date = b.write_date\r\n"
				+ "           AND a.inq_date = ?\r\n"
				+ "       union\r\n"
				+ "        SELECT ?, SUM(a.day_views), SUM(b.day_writes) \r\n"
				+ "          FROM t_inq_cnt a, t_ans_cnt b\r\n"
				+ "         WHERE a.inq_date = b.write_date\r\n"
				+ "           AND a.inq_date = ?\r\n"
				+ "       union\r\n"
				+ "        SELECT ?, SUM(a.day_views), SUM(b.day_writes) \r\n"
				+ "          FROM t_inq_cnt a, t_ans_cnt b\r\n"
				+ "         WHERE a.inq_date = b.write_date\r\n"
				+ "           AND a.inq_date = ?";
		return jdbcTemplate.query(sql, chartListMapper());
	}
	
	@Override
	public Board postQuestion(Board board) {
		String sql = "INSERT INTO t_bbd VALUES ((select IFNULL(MAX(bbd_seq) + 1, 1) FROM t_bbd b), 0, now(), ?, ?, ?, ?, null, null, null, null, ?, ?)";
		jdbcTemplate.update(sql, board.getReg_writer(), board.getBbd_title(), board.getBbd_content(), board.getBbd_attach_1(), board.getBbd_password(), board.getInq_security_yn());
		return board;
	}
	
	@Override
	public Board postAnswer(Board board) {
		String sql = "INSERT INTO t_bbd VALUES (?, (select IFNULL(MAX(ans_seq) + 1, 1) FROM t_bbd b WHERE b.bbd_seq=?), now(), ?, ?, ?, ?, null, null, null, null, ?, ?)";
		jdbcTemplate.update(sql, board.getBbd_seq(), board.getBbd_seq(),  board.getReg_writer(), board.getBbd_title(), board.getBbd_content(), board.getBbd_attach_1(), board.getBbd_password(), board.getInq_security_yn());
		return board;
	}
	
	//transaction 처리 해야함
	@Override
	public String deleteBoard(Long bbdId, Long ansId) {		
		jdbcTemplate.update("DELETE FROM t_bbd WHERE bbd_seq=? AND ans_seq=?", bbdId, ansId);
		jdbcTemplate.update("DELETE FROM t_inq_cnt WHERE bbd_seq=? AND ans_seq=?", bbdId, ansId);
		jdbcTemplate.update("DELETE FROM t_ans_cnt WHERE bbd_seq=? AND ans_seq=?", bbdId, ansId);
		return "게시글을 삭제했습니다.";
	}


	@Override
	public Board updateBoard(Board board) {
		jdbcTemplate.update("UPDATE t_bbd SET bbd_title=?, bbd_content=?, reg_writer=?, bbd_password=?, inq_security_yn=? "
				+ "WHERE bbd_seq=? AND ans_seq=?",
				board.getBbd_title(), board.getBbd_content(), board.getReg_writer(), board.getBbd_password(), board.getInq_security_yn(),
				board.getBbd_seq(), board.getAns_seq());
		return board;
	}
	
	// 답변 있으면 질문을 지울 수 없음
	@Override
	public int checkAnswersForDelete(Long bbdId) {
		return jdbcTemplate.queryForObject("SELECT count(*) FROM t_bbd \r\n"
				+ "WHERE bbd_seq=? AND ans_seq>0", Integer.class, bbdId);
	};

	// 작성횟수++ 시에 이미 있는 아이디인지 확인하기
	@Override
	public String checkWrite() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int checkView(Long bbdId, Long ansId) {
		return jdbcTemplate.queryForObject("SELECT count(*) from t_inq_cnt WHERE inq_date=DATE_FORMAT(NOW(), '%Y-%m-%d')\r\n"
				+ "             AND bbd_seq=? AND ans_seq=? \r\n", Integer.class, bbdId, ansId);
	}

	@Override
	public String plusView(Long bbdId, Long ansId, int count) {
		if (count == 0) {
			jdbcTemplate.update("INSERT INTO t_inq_cnt VALUES(DATE_FORMAT(NOW(), '%Y-%m-%d'), ?, ?, 1)", bbdId, ansId);
		} else {
			jdbcTemplate.update("UPDATE t_inq_cnt SET day_views=day_views+1 \r\n"
					+ "		WHERE inq_date=DATE_FORMAT(NOW(), '%Y-%m-%d') AND bbd_seq=? AND ans_seq=?", bbdId, ansId);
		}
		return "day view + 1";
	}


	@Override
	public String plusWrite(Long bbdId, Long ansId) {
		jdbcTemplate.update("INSERT INTO t_ans_cnt VALUES(DATE_FORMAT(NOW(), '%Y-%m-%d'), ?, ?, 1)", bbdId, ansId);
		return "day write + 1";
	}


	
	private RowMapper<Board> questionListMapper(){
		return (rs, rowNum) -> {
			Board board = new Board();
			board.setBbd_seq(rs.getLong("bbd_seq"));
			board.setAns_seq(rs.getLong("ans_seq"));
			board.setReg_writer(Seed.decrypt(rs.getString("reg_writer")));
			board.setReg_datetime(rs.getString("reg_datetime"));
			board.setBbd_title(rs.getString("bbd_title"));
			board.setBbd_password(Seed.decrypt(rs.getString("bbd_password")));
			board.setInq_security_yn(rs.getString("inq_security_yn"));
			board.setAnswer_count(rs.getLong("answer_count"));
			board.setTotal_views(rs.getLong("total_views"));
			
			return board;
		};
	}
	
	private RowMapper<Board> boardListMapper(){
		return (rs, rowNum) -> {
			Board board = new Board();
			board.setBbd_seq(rs.getLong("bbd_seq"));
			board.setAns_seq(rs.getLong("ans_seq"));
			board.setReg_writer(Seed.decrypt(rs.getString("reg_writer")));
			board.setReg_datetime(rs.getString("reg_datetime"));
			board.setBbd_title(rs.getString("bbd_title"));
			board.setBbd_password(Seed.decrypt(rs.getString("bbd_password")));
			board.setInq_security_yn(rs.getString("inq_security_yn"));
			board.setAnswer_count(rs.getLong("answer_count"));
			board.setTotal_views(rs.getLong("total_views"));
			
			return board;
		};
	}
	
	private RowMapper<Board> boardMapper(){
		return (rs, rowNum) -> {
			Board board = new Board();
			board.setBbd_seq(rs.getLong("bbd_seq"));
			board.setAns_seq(rs.getLong("ans_seq"));
			board.setReg_writer(Seed.decrypt(rs.getString("reg_writer")));
			board.setReg_datetime(rs.getString("reg_datetime"));
			board.setBbd_title(rs.getString("bbd_title"));
			board.setBbd_content(rs.getString("bbd_content"));
			board.setBbd_password(Seed.decrypt(rs.getString("bbd_password")));
			board.setInq_security_yn(rs.getString("inq_security_yn"));
			board.setAnswer_count(rs.getLong("answer_count"));
			board.setTotal_views(rs.getLong("total_views"));
			
			return board;
		};
	}
	
	private RowMapper<Board> answerListMapper(){
		return (rs, rowNum) -> {
			Board board = new Board();
			board.setBbd_seq(rs.getLong("bbd_seq"));
			board.setAns_seq(rs.getLong("ans_seq"));
			board.setReg_writer(Seed.decrypt(rs.getString("reg_writer")));
			board.setReg_datetime(rs.getString("reg_datetime"));
			board.setBbd_title(rs.getString("bbd_title"));
			board.setBbd_password(Seed.decrypt(rs.getString("bbd_password")));
			board.setInq_security_yn(rs.getString("inq_security_yn"));
			board.setTotal_views(rs.getLong("total_views"));
			
			return board;
		};
	}
	
	private RowMapper<BoardStatus> chartListMapper(){
		return (rs, rowNum) -> {
			BoardStatus status = new BoardStatus();
			status.setDate(rs.getString("date"));
			status.setDailyView(rs.getLong("dailyView"));
			status.setDailyWrite(rs.getLong("dailyWrite"));
			
			return status;
		};
	}


	
	
}

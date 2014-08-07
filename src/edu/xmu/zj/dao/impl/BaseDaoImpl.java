/** 
 *BaseDaoImpl.java 2009-11-8 xjb
 */
package edu.xmu.zj.dao.impl;

import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;

import edu.xmu.zj.dao.BaseDao;
import edu.xmu.zj.pojo.Task;

/**
 * @author xjb
 */
public class BaseDaoImpl implements BaseDao {

	private static Logger logger = Logger.getLogger(BaseDaoImpl.class);

	private static SqlMapClient sqlMapper;

	static {
		try {
			Reader reader = Resources
					.getResourceAsReader("config/SqlMapConfig.xml");
			sqlMapper = SqlMapClientBuilder.buildSqlMapClient(reader);
			reader.close();
		} catch (IOException e) {
			// Fail fast.
			throw new RuntimeException(
					"Something bad happened while building the SqlMapClient instance."
							+ e, e);
		}
	}

	/**
	 * @function 鏍规嵁sql鏂规硶鍚嶇О鍜屽璞℃彃鍏ユ暟鎹簱
	 * @param sqlName
	 *            sql鏂规硶鍚嶇О
	 * @param obj
	 *            瀵硅薄
	 * @return Object 瀵硅薄
	 */
	public void save(String sqlName, Object obj) {
		try {
			sqlMapper.startBatch();
			sqlMapper.insert(sqlName, obj);
			sqlMapper.executeBatch();
		} catch (SQLException e) {
			 logger.error("xxxxxxxxxxxxxxxxxxxx", e);
		}
	}

	/**
	 * @function 鏍规嵁sql鏂规硶鍚嶇О鍜屽璞′慨鏀规暟鎹簱
	 * @param sqlName
	 *            sql鏂规硶鍚嶇О
	 * @param obj
	 *            瀵硅薄
	 * @return void
	 */
	public void update(String sqlName, Object obj) {
		try {
			sqlMapper.startBatch();
			sqlMapper.update(sqlName, obj);
			sqlMapper.executeBatch();
		} catch (SQLException e) {

			// logger.error("鎿嶄綔鏁版嵁搴撻敊璇�閿欒锛筹急锛负锛� + obj, e);
		}
	}

	/**
	 * @function 灏嗘壒閲忔暟鎹彃鍏ユ暟鎹簱涓�
	 * @param sqlName
	 *            sql鏂规硶鍚嶇О
	 * @param list
	 *            瀵硅薄
	 * @return void
	 */
	public void insertList(String sqlName, List list) {

		try {
			logger.debug("ibatis批量插入开始");
			// sqlMapper.startTransaction();
			sqlMapper.startBatch();
			Iterator it = list.iterator();
			while (it.hasNext()) {
				Object obj = it.next();
				sqlMapper.update(sqlName, obj);
			}
			sqlMapper.executeBatch();
			logger.debug("ibatis批量插入结束");
		} catch (SQLException e) {
		}

	}

	/**
	 * @function 灏嗘壒閲忔暟鎹洿鏂板埌鏁版嵁搴撲腑
	 * @param sqlName
	 *            sql鏂规硶鍚嶇О
	 * @param list
	 *            瀵硅薄
	 * @return void
	 */
	public void updateList(String sqlName, List list) {
		insertList(sqlName, list);
	}

	/**
	 * @function 鏍规嵁sql鏂规硶鍚嶇О鍜屽璞d
	 * @param sqlName
	 *            sql鏂规硶鍚嶇О
	 * @param id
	 *            id瀵硅薄
	 * @return void
	 */
	public void removeObject(String sqlName, Object id) {
		try {
			sqlMapper.startBatch();
			sqlMapper.delete(sqlName, id);
			sqlMapper.executeBatch();
		} catch (SQLException e) {

			// logger.error("鎿嶄綔鏁版嵁搴撻敊璇�, e);
		}
	}

	/**
	 * @function 鏍规嵁sql鏂规硶鍚嶇О鍜屽璞d
	 * @param sqlName
	 *            sql鏂规硶鍚嶇О
	 * @param ids
	 *            ids瀵硅薄鏁扮粍
	 * @return void
	 */
	public void removeObjects(String sqlName, Object[] ids) {
		try {
			sqlMapper.startBatch();
			for (int i = 0; i < ids.length; i++) {
				Object id = ids[i];
				sqlMapper.delete(sqlName, id);

			}
			sqlMapper.executeBatch();
		} catch (SQLException e) {

			// logger.error("鎿嶄綔鏁版嵁搴撻敊璇�, e);
		}
	}

	/**
	 * @function 鏍规嵁sql鏂规硶鍚嶇О鍜屾潯浠跺弬鏁帮紙HashMap锛夊彇鍥炴煡璇㈢粨鏋滃垪琛�
	 * @param sqlName
	 *            sql鏂规硶鍚嶇О
	 * @param param
	 *            hm 鏉′欢鍙傛暟鐨勫�
	 * @return List 鍒楄〃
	 */
	public List getList(String sqlName, HashMap hm) {
		try {
			return sqlMapper.queryForList(sqlName, hm);
		} catch (SQLException e) {

			// logger.error("鎿嶄綔鏁版嵁搴撻敊璇�, e);
		}
		return null;
	}

	/**
	 * @function 鏍规嵁sql鏂规硶鍚嶇О鍜屼富閿�锛屽彇鍥炴煡璇㈢粨鏋滃垪琛�
	 * @param sqlName
	 *            sql鏂规硶鍚嶇О
	 * @param param
	 *            obj 涓婚敭鐨勫�
	 * @return List 鍒楄〃
	 */
	public List getList(String sqlName, Object obj) {
		try {
			return sqlMapper.queryForList(sqlName, obj);
		} catch (SQLException e) {

			// logger.error("鎿嶄綔鏁版嵁搴撻敊璇�, e);
		}
		return null;
	}

	/**
	 * @function 鏍规嵁sql鏂规硶鍚嶇О鍙栧洖鏌ヨ缁撴灉鍒楄〃
	 * @param sqlName
	 *            sql鏂规硶鍚嶇О
	 * @return List 鍒楄〃
	 */
	public List getList(String sqlName) {
		try {
			return sqlMapper.queryForList(sqlName);
		} catch (SQLException e) {

			// logger.error("鎿嶄綔鏁版嵁搴撻敊璇�, e);
		}
		return null;
	}

	/**
	 * @function 鑾峰彇HASHMAP
	 * @param sqlName
	 *            sql鏂规硶鍚嶇О
	 * @param obj
	 *            瀵硅薄
	 * @param keyFiled
	 *            涓婚敭
	 * @param valueField
	 *            鍊�
	 * @return Map
	 */
	public Map getMap(String sqlName, Object obj, String keyFiled,
			String valueField) {
		try {
			return sqlMapper.queryForMap(sqlName, obj, keyFiled, valueField);
		} catch (SQLException e) {

			// logger.error("鎿嶄綔鏁版嵁搴撻敊璇�, e);
		}
		return null;
	}

	/**
	 * @function 鏍规嵁sql鏂规硶鍚嶇О鍜屾潯浠跺弬鏁帮紙HashMap锛夊彇鍥炴煡璇㈢粨鏋滃璞�
	 * @param sqlName
	 *            sql鏂规硶鍚嶇О
	 * @param hm
	 *            鏉′欢鍙傛暟鐨勫�
	 * @return Object
	 */
	public Object getObject(String sqlName, HashMap hm) {
		try {
			return sqlMapper.queryForObject(sqlName, hm);
		} catch (SQLException e) {

			// logger.error("鎿嶄綔鏁版嵁搴撻敊璇�, e);
		}
		return null;
	}

	/**
	 * @function 鏍规嵁sql鏂规硶鍚嶇О鍜屼富閿�锛屽彇鍥炴煡璇㈢粨鏋滃璞�
	 * @param sqlName
	 *            sql鏂规硶鍚嶇О
	 * @param obj
	 *            瀵硅薄
	 * @return Object
	 */
	public Object getObject(String sqlName, Object obj) {
		try {
			return sqlMapper.queryForObject(sqlName, obj);
		} catch (SQLException e) {

		}
		return null;
	}

	/**
	 * @function 鏍规嵁sql鏂规硶鍚嶇О锛屽彇鍥炴煡璇㈢粨鏋滃璞�
	 * @param sqlName
	 *            sql鏂规硶鍚嶇О
	 * @return Object
	 */
	public Object getObject(String sqlName) {
		try {
			return sqlMapper.queryForObject(sqlName);
		} catch (SQLException e) {
			// logger.error("鎿嶄綔鏁版嵁搴撻敊璇�, e);
		}
		return null;
	}

	public static void main(String[] arg) {
		BaseDao dao = new BaseDaoImpl();
		// List<Task> list = dao.getList("Select.getAllTask");
		Task task = (Task) dao.getObject("Select.getTaskByTaskID", "1");
		System.out.println(task.getUrl());
	}

}

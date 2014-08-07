/** 
  *BaseDao.java 2009-11-8 xjb
  */
package edu.xmu.zj.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @function   
 * @author xjb
 */
public interface BaseDao {
	
	/**
	 * @function 根据sql方法名称和对象插入数据库
	 * @param sqlName sql方法名称
	 * @param obj 对象
	 * @return Object 对象
	 */
	public void save(String sqlName, Object obj);
	
	/**
	 * @function 根据sql方法名称和对象修改数据库
	 * @param sqlName sql方法名称
	 * @param obj 对象
	 * @return void
	 */
	public void update(String sqlName, Object obj);
	
	
	/**
	 * @function 将批量数据插入数据库中
	 * @param sqlName sql方法名称
	 * @param list 对象
	 * @return void
	 */
	public void insertList(String sqlName, List list);
	
    /**
	 * @function 将批量数据更新到数据库中
	 * @param sqlName sql方法名称
	 * @param list 对象
	 * @return void
	 */
	public void updateList(String sqlName, List list) ;
	
 
	/**
	 * @function 根据sql方法名称和对象id
	 * @param sqlName sql方法名称
	 * @param  id id对象
	 * @return void
	 */
	public void removeObject(String sqlName, Object id) ;
 
	/**
	 * @function 根据sql方法名称和对象id
	 * @param sqlName sql方法名称
	 * @param ids ids对象数组
	 * @return void
	 */
	public void removeObjects(String sqlName, Object[] ids) ;
	
	 
	/**
	 * @function 根据sql方法名称和条件参数（HashMap）取回查询结果列表
	 * @param sqlName sql方法名称
	 * @param param hm 条件参数的值
	 * @return List 列表
	 */
	public List getList(String sqlName, HashMap hm);
 
	/**
	 * @function 根据sql方法名称和主键值，取回查询结果列表
	 * @param sqlName sql方法名称
	 * @param param obj 主键的值
	 * @return List 列表
	 */
	public List getList(String sqlName, Object obj);

	 
	/**
	 * @function 根据sql方法名称取回查询结果列表
	 * @param sqlName sql方法名称 
	 * @return  List 列表
	 */
	public List getList(String sqlName);
	 
	/**
	 * @function 获取HASHMAP
	 * @param sqlName sql方法名称
	 * @param obj 对象
	 * @param keyFiled 主键
	 * @param valueField 值
	 * @return Map
	 */
	public Map getMap(String sqlName,Object obj,String keyFiled,String valueField) ;

	 
	/**
	 * @function 根据sql方法名称和条件参数（HashMap）取回查询结果对象
	 * @param sqlName sql方法名称
	 * @param hm 条件参数的值
	 * @return Object
	 */
	public Object getObject(String sqlName, HashMap hm);

	 
	/**
	 * @function 根据sql方法名称和主键值，取回查询结果对象
	 * @param sqlName sql方法名称
	 * @param obj 对象
	 * @return Object
	 */
	public Object getObject(String sqlName, Object obj);
 
	/**
	 * @function 根据sql方法名称，取回查询结果对象
	 * @param sqlName sql方法名称 
	 * @return Object
	 */
	public Object getObject(String sqlName) ;


}

package edu.xmu.zj.process;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import edu.xmu.zj.pojo.Task;
import edu.xmu.zj.util.TypeSort;


/**
 * 分析工厂类，根据任务类型产生实际解析类
 * 
 * @date 2009-04-23
 * 
 */
public class AnalyseFactory {
	private Logger logger = Logger.getLogger(AnalyseFactory.class);

	private Task task;

	private Map<String, String> typeMapping = new HashMap<String, String>();// 类型映射

	private String configPath = this.getClass().getResource("/").getPath()
			.replaceAll("%20", " ")
			+ "type-mapping.xml";// 类型映射文件地址

	public AnalyseFactory(Task task) {
		this.task = task;
	}

	/**
	 * 获取分析实例，若类型对应列表为空，则读取配置
	 * 
	 * @return TaskAnalyseAbs子类实例
	 * @throws ClassNotFoundException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	@SuppressWarnings("unchecked")
	public TaskAnalyseAbs getAnalyser(String url)
			throws ClassNotFoundException, SecurityException,
			NoSuchMethodException, IllegalArgumentException,
			InstantiationException, IllegalAccessException,
			InvocationTargetException {
		if (typeMapping.size() == 0) {
			this.loadTypeConfig();
			if (typeMapping.size() == 0) {
				throw new IllegalArgumentException("配置文件中没有任何解析类型");
			}
		}

		String clazzName = typeMapping.get(this.task.getType());
		if (clazzName == null) {
			throw new ClassNotFoundException("配置文件中不存在对应类型为 '"
					+ this.task.getType() + "' 的解析类");
		}

		// 构造类的构造函数参数列表
		Class[] constructorParams = new Class[] { Task.class, String.class};
		Object[] paras = new Object[] { this.task, url};
		Class Object = Class.forName(clazzName);

		// 构造函数
		Constructor constr = Object.getConstructor(constructorParams);

		// 获得实例，并指定到抽象父类
		return (TaskAnalyseAbs) constr.newInstance(paras);
	}

	/**
	 * 根据任务URL分析获取任务类型
	 * 
	 * @return TaskAnalyseAbs子类实例
	 * @throws ClassNotFoundException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	@SuppressWarnings("unchecked")
	public TaskAnalyseAbs getAnalyser(Task task)
			throws ClassNotFoundException, SecurityException,
			NoSuchMethodException, IllegalArgumentException,
			InstantiationException, IllegalAccessException,
			InvocationTargetException {
		this.loadTypeConfig();
		
		if (typeMapping.size() == 0) {
			throw new IllegalArgumentException("配置文件中没有任何解析类型");
		}

		String clazzName = typeMapping.get(task.getType());
		if (clazzName == null) {
			throw new ClassNotFoundException("配置文件中不存在对应类型为 '"
					+ task.getType() + "' 的解析类");
		}

		// 构造类的构造函数参数列表
		Class[] constructorParams = new Class[] { Task.class, String.class};
		Object[] paras = new Object[] { task, task.getUrl()};

		Class Object = Class.forName(clazzName);

		// 构造函数
		Constructor constr = Object.getConstructor(constructorParams);

		// 获得实例，并指定到抽象父类
		return (TaskAnalyseAbs) constr.newInstance(paras);
	}

	/**
	 * 刷新类型对应列表，并返回实际分析类
	 * 
	 * @param reload
	 *            true为重新读取类型对应列表
	 * @return
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public TaskAnalyseAbs getAnalyser(String url, boolean reload)
			throws SecurityException, IllegalArgumentException,
			ClassNotFoundException, NoSuchMethodException,
			InstantiationException, IllegalAccessException,
			InvocationTargetException {
		if (reload) {
			this.loadTypeConfig();
		}
		return this.getAnalyser(url);
	}

	/**
	 * 读取配置文件
	 */
	@SuppressWarnings("unchecked")
	private void loadTypeConfig() {
		SAXReader reader = new SAXReader();
		Document doc = null;
		Iterator it1 = null;
		Element ele1 = null;

		try {
			doc = reader.read(new File(configPath));

			for (it1 = doc.getRootElement().elementIterator("process-mapping"); it1
					.hasNext();) {
				ele1 = (Element) it1.next();

				String typename = ele1.elementTextTrim("typename");
				String classname = ele1.elementTextTrim("classname");

				typeMapping.put(typename, classname);
			}
		} catch (DocumentException e) {
			logger.error("配置文件不存在或格式读取异常",e);
		}
	}

	private Map<String, String> getTypeMapping() {
		this.loadTypeConfig();
		return typeMapping;
	}
	
	public List<String> getAnalyserType(){
		List<String> r=new ArrayList<String>();
		r.addAll(getTypeMapping().keySet());
		Collections.sort(r,new TypeSort());
		return r;
	}
}

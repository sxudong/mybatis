/*
 *    Copyright 2009-2012 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.session;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Properties;

import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.exceptions.ExceptionFactory;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;

/*
 * Builds {@link SqlSession} instances.
 * 构建SqlSessionFactory的工厂.工厂模式
 *
 */
/**
 * @author Clinton Begin
 */
public class SqlSessionFactoryBuilder {

  //SqlSessionFactoryBuilder有9个build()方法
  //发现mybatis文档老了,http://www.mybatis.org/core/java-api.html,关于这块对不上

  //以下3个方法都是调用下面第4种方法
  public SqlSessionFactory build(Reader reader) {
    return build(reader, null, null);
  }

  public SqlSessionFactory build(Reader reader, String environment) {
    return build(reader, environment, null);
  }

  public SqlSessionFactory build(Reader reader, Properties properties) {
    return build(reader, null, properties);
  }

  //第4种方法是最常用的，它使用了一个参照了XML文档或更特定的SqlMapConfig.xml文件的Reader实例。
  //可选的参数是environment和properties。Environment决定加载哪种环境(开发环境/生产环境)，包括数据源和事务管理器。
  //如果使用properties，那么就会加载那些properties（属性配置文件），那些属性可以用${propName}语法形式多次用在配置文件中。和Spring很像，一个思想？
  public SqlSessionFactory build(Reader reader, String environment, Properties properties) {
    try {
      //委托 XMLConfigBuilder 来解析xml文件，并构建
      XMLConfigBuilder parser = new XMLConfigBuilder(reader, environment, properties);
      // 这个返回的是 SqlSessionFactory，从这一行代码可以看出明面上是创建 Sqlsession 等对象,
      // 实际上是初始化 Configuration 然后再串联不同的处理层，比如 sqlsession, methodproxy,
      // mapperproxy,statement等
      return build(parser.parse()); // parser.parse()这个方法其实返回的就是Configuration对象
    } catch (Exception e) {
        //这里是捕获异常，包装成自己的异常并抛出的idiom？，最后还要reset ErrorContext
      throw ExceptionFactory.wrapException("Error building SqlSession.", e);
    } finally {
      ErrorContext.instance().reset();
      try {
        reader.close();
      } catch (IOException e) {
        // Intentionally ignore. Prefer previous error.
      }
    }
  }

  //以下3个方法都是调用下面第8种方法
  public SqlSessionFactory build(InputStream inputStream) {
    return build(inputStream, null, null);
  }

  public SqlSessionFactory build(InputStream inputStream, String environment) {
    return build(inputStream, environment, null);
  }

  public SqlSessionFactory build(InputStream inputStream, Properties properties) {
    return build(inputStream, null, properties);
  }

  //第8种方法和第4种方法差不多，Reader换成了InputStream
  public SqlSessionFactory build(InputStream inputStream, String environment, Properties properties) {
    try {
      XMLConfigBuilder parser = new XMLConfigBuilder(inputStream, environment, properties);
      return build(parser.parse());
    } catch (Exception e) {
      throw ExceptionFactory.wrapException("Error building SqlSession.", e);
    } finally {
      ErrorContext.instance().reset();
      try {
        inputStream.close();
      } catch (IOException e) {
        // Intentionally ignore. Prefer previous error.
      }
    }
  }
    
  //最后一个build方法使用了一个Configuration作为参数,并返回DefaultSqlSessionFactory
  public SqlSessionFactory build(Configuration config) {
    // sqlSessionManager 的创建过程依赖 SqlSessionFactory对象，这里会先创建一个
    // 默认的 sqlSession工厂，但是会依赖事先初始化好的 Configuration对象 去创建.
    return new DefaultSqlSessionFactory(config);
  }

}

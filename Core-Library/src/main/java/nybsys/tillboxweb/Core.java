/**
 * Created By: Md. Nazmus Salahin
 * Created Date: 12-Jan-18
 * Time: 4:53 PM
 * Modified By:
 * Modified date:
 * (C) CopyRight Nybsys ltd.
 */
package nybsys.tillboxweb;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import nybsys.tillboxweb.MessageModel.*;
import nybsys.tillboxweb.Utils.TillBoxUtils;
import nybsys.tillboxweb.appenum.TillBoxAppEnum;
import nybsys.tillboxweb.broker.client.CallBack;
import nybsys.tillboxweb.broker.client.PublisherForRollBackAndCommit;
import nybsys.tillboxweb.broker.client.PublisherForWorker;
import nybsys.tillboxweb.broker.client.SubscriberForWorker;
import nybsys.tillboxweb.constant.WorkerSubscriptionConstants;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.hibernate.SessionFactory;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

public abstract class Core {
    private static final Logger log = LoggerFactory.getLogger(Core.class);
    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    protected static final ObjectMapper jsonMapper = new ObjectMapper()
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT.FAIL_ON_UNKNOWN_PROPERTIES, false);
            //.setDateFormat(dateFormat);

    protected static final ModelMapper modelMapper = new ModelMapper();

    public static final ThreadLocal<Class> runTimeModelType = new ThreadLocal<>();
    public static final ThreadLocal<Class> runTimeEntityType = new ThreadLocal<>();
    //public static final ThreadLocal<Session> sessionThreadLocal = new ThreadLocal<>();
    public static final ThreadLocal<SessionFactory> sessionFactoryThreadLocal = new ThreadLocal<>();

    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    public static final Validator validator = factory.getValidator();

    public CyclicBarrier barrier;

    public static BaseHistoryEntity HistoryEntity;
    public static final ThreadLocal<String> messageId = new ThreadLocal<>();

    public static final PublisherForRollBackAndCommit publisherForRollBackAndCommit
            = new PublisherForRollBackAndCommit();

    public static final ThreadLocal<ClientMessage> clientMessage = new ThreadLocal<>();
    public static final ThreadLocal<String> userId = new ThreadLocal<>();
    public static final ThreadLocal<Integer> businessId = new ThreadLocal<>();
    public static final ThreadLocal<String> userDataBase = new ThreadLocal<>();
    public static final ThreadLocal<String> requestToken = new ThreadLocal<>();


    // Currency value ===========================================================
    public static final ThreadLocal<Integer> baseCurrencyID = new ThreadLocal<>();
    public static final ThreadLocal<Integer> entryCurrencyID = new ThreadLocal<>();
    public static final ThreadLocal<Double> exchangeRate = new ThreadLocal<>();
    //===========================================================================

    public static final Map<String,SecurityResMessage> securityResponseCollection;
    static
    {
        securityResponseCollection = Collections.synchronizedMap(new HashMap<String, SecurityResMessage>());
    }

    //public static SessionFactory sessionFactoryForModule;

    //public final long allowedTime = TimeUnit.NANOSECONDS.convert(30, TimeUnit.SECONDS);

    //Wait for 30 second
    public static final Integer allowedTime = 30000;
    //public final Integer allowedTime = 10000;
    private static final Long nanoSecond = TimeUnit.NANOSECONDS.convert(allowedTime, TimeUnit.MILLISECONDS);

    //@Autowired
    static AnnotationConfigApplicationContext applicationContext;
    static
    {
         applicationContext =
                    new AnnotationConfigApplicationContext();

            applicationContext.scan("nybsys.tillboxweb.dbConfig");
            applicationContext.refresh();
    }

    /**
     * CREATE DATA SOURCE
     *
     * @param dataBaseName *
     * @return entity manager instance
     * @throws IllegalStateException if the entity manager factory
     */
    public void selectDataBase(String dataBaseName) {
        //log.info("Execute Thread: " + Thread.currentThread().getName().toString());

        SessionFactory sessionFactory;
        //Session session;
        DataSource dataSource;

        try {
           /* AnnotationConfigApplicationContext applicationContext =
                    new AnnotationConfigApplicationContext();

            applicationContext.scan("com.nybsys.tillboxweb.dbConfig");
            applicationContext.refresh();*/

            //log.warn("Data base changing... Name: " + dataBaseName);

            dataSource = (DataSource) this.applicationContext.getBean("dataSource", dataBaseName);
            sessionFactory = (SessionFactory) this.applicationContext.getBean("sessionFactory", dataSource);

            //log.warn("Data base changed");
            //TillBoxUtils.changeDataBase(applicationContext,dataSource,sessionFactory);

            //session = sessionFactory.openSession();
            //sessionThreadLocal.set(session);
            sessionFactoryThreadLocal.remove();
            sessionFactoryThreadLocal.set(sessionFactory);
            //Core.commonDataBase.set(false);
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error("Error in Database connection", ex);
            throw ex;
        }
    }

    public void setDefaultDateBase() {
        try {
            SessionFactory sessionFactory;
            //Session session;
            sessionFactory = (SessionFactory) this.applicationContext.getBean("defaultSessionFactory");
            //session = sessionFactory.openSession();
            sessionFactoryThreadLocal.set(sessionFactory);
            //Core.commonDataBase.set(true);
            //sessionThreadLocal.set(session);
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error("Error of getting session factory"+ex.getMessage());
        }
    }

    public static <T> T getRequestObject(RequestMessage requestMessage, Class clazz) {
        Object convertedObject;
        Object requestObject=null;
        try {
            if(requestMessage.requestObj!=null)
                requestObject = requestMessage.requestObj;

            convertedObject = Core.jsonMapper.convertValue(requestObject, clazz);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
        return (T) convertedObject;
    }

    public static <T> T getResponseObject(ResponseMessage requestMessage, Class clazz) {
        Object convertedObject;
        Object responseObject=null;
        try {
            if(requestMessage.responseObj!=null)
                responseObject = requestMessage.responseObj;

            convertedObject = Core.jsonMapper.convertValue(responseObject, clazz);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
        return (T) convertedObject;
    }

    /*public static <T extends BaseModel> T getRequestObject(RequestMessage requestMessage) {
        Object convertedObject = null;
        try {
            Object requestObj = requestMessage.requestObj;
            Class clazz = Core.runTimeModelType.get();
            //clazz.newInstance();

            convertedObject = jsonMapper.convertValue(requestObj, clazz);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
        return (T) convertedObject;
    }*/

    public static ResponseMessage buildDefaultResponseMessage() {
        ResponseMessage responseMessage = new ResponseMessage();
        return responseMessage;
    }

    public static RequestMessage getDefaultWorkerRequestMessage() {
        RequestMessage requestMessage = new RequestMessage();
        requestMessage.brokerMessage = new BrokerMessage();
        requestMessage.brokerMessage.requestFrom =
                TillBoxAppEnum.BrokerRequestType.WORKER.get();
        requestMessage.brokerMessage.messageId = TillBoxUtils.getUUID();
        return requestMessage;
    }

    public static Map getKeyValuePairFromObject(Object obj) {
        return getKeyValuePairFromObject(obj, null);
    }

    public static Map getKeyValuePairFromObject(Object obj, Integer queryType) {
        Map<Object, Object> keyValue = new HashMap<>();
        ReflectionUtils.doWithFields(obj.getClass(), field -> {
            try {
                String type = field.getType().toString();
                type = org.apache.commons.lang3.StringUtils.substringAfterLast(type, "class").trim();

                //Object something = "1";
                //Object result= TillBoxUtils.castValue(type.trim(),something);


                boolean condition1 = true, condition2, condition3, condition4, condition5;
                //System.out.println("Field name: " + field.getName());
                field.setAccessible(true);
                //System.out.println("Field value: "+ field.get(obj));
                condition2 = !ObjectUtils.isEmpty(field.get(obj));
                condition3 = !StringUtils.isEmpty(field.get(obj));
                //if (condition3)
                //    condition1 = !field.get(obj).toString().equals("0");
                // condition4 = !StringUtils.startsWithIgnoreCase(field.getName().toString(), "created");
                // condition5 = !StringUtils.startsWithIgnoreCase(field.getName().toString(), "updated");
                if (condition3 && condition2) {
                    if (queryType != null)
                        keyValue.put(field.getName().toString() + queryType, TillBoxUtils.castValue(type, field.get(obj)));
                    else
                        keyValue.put(field.getName().toString(), TillBoxUtils.castValue(type, field.get(obj)));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                throw ex;
            }

        });
        return keyValue;
    }


    /**
     * BUILD SELECT, UPDATE OR DELETE QUERY WITH WHERE CONDITION
     *
     * @param whereCondition *
     * @param queryType      *
     * @return build query string *
     * @throws Exception if there is any error in building query
     */
    public String queryBuilder(Object whereCondition, int queryType) throws Exception {
        String buildQuery;
        buildQuery = this.queryBuilder(whereCondition, queryType, null);
        return buildQuery;
    }


    /**
     * BUILD SELECT, UPDATE OR DELETE QUERY WITH WHERE CONDITION
     *
     * @param whereCondition *
     * @param queryType      *
     * @param entityName     *
     * @return build query string *
     * @throws Exception if there are any error in building query
     */
    public String queryBuilder(Object whereCondition, int queryType, String entityName) throws Exception {
        return queryBuilder(whereCondition, queryType, null, entityName);
    }

    public String queryBuilder(Object whereCondition, int queryType, Object updateObject) throws Exception {
        return queryBuilder(whereCondition, queryType, updateObject, null);
    }

    public String queryBuilder(Object whereCondition, int queryType, Object updateObject, String entityName) throws Exception {
        Map<Object, Object> keyValueParisForWhereCondition;
        Map<Object, Object> keyValuePairsForUpdate;
        Map<Object, Object> keyValuePairsWhereConditionForDelete;
        StringBuilder query;
        try {
            Class clazz = Core.runTimeEntityType.get();
            if (entityName == null) {
                entityName = clazz.getName();
                //entityName = org.apache.commons.lang3.StringUtils.substringAfterLast(entityName, ".").trim();
                keyValueParisForWhereCondition = Core.getKeyValuePairFromObject(whereCondition);
            } else {
                keyValueParisForWhereCondition = (Map<Object, Object>) whereCondition;
            }


            query = new StringBuilder();

            if (TillBoxAppEnum.QueryType.Select.get() == queryType) {
                query.append("SELECT t ")
                        .append("FROM " + entityName + " t ")
                        .append(" WHERE ");
                query = this.criteriaBuilder(keyValueParisForWhereCondition, query, TillBoxAppEnum.QueryType.Select.get());
            }

            if (TillBoxAppEnum.QueryType.Delete.get() == queryType) {
                query.append("DELETE FROM ");
                query.append("" + entityName + " t ");
                keyValuePairsWhereConditionForDelete = Core.getKeyValuePairFromObject(whereCondition);
                query.append(" WHERE ");
                query = this.criteriaBuilder(keyValuePairsWhereConditionForDelete, query, TillBoxAppEnum.QueryType.Select.get());
            }

            if (TillBoxAppEnum.QueryType.Update.get() == queryType) {
                query.append("UPDATE ");
                query.append("" + entityName + " t ");
                query.append("set ");
                keyValuePairsForUpdate = Core.getKeyValuePairFromObject(updateObject);
                query = this.criteriaBuilder(keyValuePairsForUpdate, query, TillBoxAppEnum.QueryType.Update.get());
                query.append(" WHERE ");
                query = this.criteriaBuilder(keyValueParisForWhereCondition, query, TillBoxAppEnum.QueryType.Select.get());

            }

            if (TillBoxAppEnum.QueryType.CountRow.get() == queryType) {
                query.append("SELECT COUNT(*) FROM ");
                query.append("" + entityName + " t");
                query.append(" WHERE ");
                query = this.criteriaBuilder(keyValueParisForWhereCondition, query, TillBoxAppEnum.QueryType.Select.get());

            }

        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
        return query.toString();
    }

    private StringBuilder criteriaBuilder(Map<Object, Object> keyValueParis, StringBuilder query, int queryType) throws Exception {
        String key;
        try {
            List<String> criteria = new ArrayList<String>();

            for (Map.Entry<Object, Object> entry : keyValueParis.entrySet()) {
                key = entry.getKey().toString();
                criteria.add("t." + key + " = :" + key + queryType);
            }

            if (criteria.size() == 0) {
                throw new RuntimeException("no criteria");
            }
            for (int i = 0; i < criteria.size(); i++) {
                if (i > 0) {
                    if (queryType == TillBoxAppEnum.QueryType.Select.get())
                        query.append(" AND ");
                    if (queryType == TillBoxAppEnum.QueryType.Update.get())
                        query.append(" , ");
                }
                query.append(criteria.get(i));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
        return query;
    }

    public static boolean isResponseWithInAllowedTime(long startTime) {
        return isResponseWithInAllowedTime(startTime, Core.allowedTime);
    }

    public static boolean isResponseWithInAllowedTime(long startTime, long allowedTime) {
        //Long nanoSecond = TimeUnit.NANOSECONDS.convert(allowedTime,TimeUnit.MILLISECONDS);
        if ((System.nanoTime() - startTime) > Core.nanoSecond)
            return false;
        else
            return true;
    }

    public static void closeBrokerClient(MqttClient mqttClient, String subTopic) throws MqttException {
        try {
            if (mqttClient.isConnected()) {
                mqttClient.unsubscribe(subTopic);
                mqttClient.disconnect();
                mqttClient.close();
            }
        } catch (MqttException e) {
            e.printStackTrace();
            log.error("Exception from closeBrokerClient Method");
            throw e;
        }
    }

    public void rollBack() {
        String messageId = Core.messageId.get();
        Core.publisherForRollBackAndCommit.publishedMessageForRollBack(messageId);
    }

    public void commit() {
        String messageId = Core.messageId.get();
        Core.publisherForRollBackAndCommit.publishedMessageForCommit(messageId);
    }

    public static <M> List<M> convertResponseToList(ResponseMessage responseMessage, M model) throws Exception {
        List<M> finalList = new ArrayList<>();
        List tempList;
        try {
            if (responseMessage.responseObj != null) {
                tempList = (List) responseMessage.responseObj;
                if (tempList.size() > 0) {
                    for (Object object : tempList) {
                        object = Core.modelMapper.map(object, model.getClass());
                        finalList.add((M) object);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error("Exception from Core convertResponseToList method");
            throw ex;
        }
        return finalList;
    }

    public static <M> M interModuleCommunication(String publishedTopic, String serviceUrl, Object requestObject, Class responseObjectClass){

        CyclicBarrier barrier;
        MqttClient mqttClient;
        CallBack callBackForIMC;
        ResponseMessage responseMessage;
        RequestMessage requestMessage = Core.getDefaultWorkerRequestMessage();
        boolean workCompleteWithInAllowTime;
        Object lock = new Object();
        Object responseObject=null;

        try {

            barrier = TillBoxUtils.getBarrier(1, lock);
            requestMessage.brokerMessage.serviceName = serviceUrl;
            requestMessage.requestObj = requestObject;
            SubscriberForWorker subForWorker = new SubscriberForWorker(requestMessage.brokerMessage.messageId, barrier);
            mqttClient = subForWorker.subscribe();
            callBackForIMC = subForWorker.getCallBack();
            PublisherForWorker publisherForWorker = new PublisherForWorker(publishedTopic, mqttClient);
            publisherForWorker.publishedMessageToWorker(requestMessage);

            synchronized (lock) {
                long startTime = System.nanoTime();
                lock.wait(allowedTime);
                workCompleteWithInAllowTime = Core.isResponseWithInAllowedTime(startTime);

                if (workCompleteWithInAllowTime) {

                    responseMessage = callBackForIMC.getResponseMessage();
                    responseObject = Core.getResponseObject(responseMessage, responseObjectClass);

                } else {
                    log.info("Response time out");
                }
            }

            Core.closeBrokerClient(mqttClient, requestMessage.brokerMessage.messageId);

        }catch (Exception ex){
            ex.printStackTrace();
        }

        return (M)responseObject;
    }
}

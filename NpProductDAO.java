package com.nextel.dao;

import com.nextel.aditional.BeanCoveragePointOfSale;
import com.nextel.aditional.ProductNotActivation;
import com.nextel.aditional.SkuProductxIdRetail;
import com.nextel.bean.NpAvailableProduct;
import com.nextel.bean.NpDevolutionDetailProm;
import com.nextel.bean.NpDevolutionProm;
import com.nextel.bean.NpLiquidation;
import com.nextel.bean.NpPlan;
import com.nextel.bean.NpPreviewConsult;
import com.nextel.bean.NpProduct;
import com.nextel.bean.NpRetailer;
import com.nextel.bean.NpSubWarehouseDetail;
import com.nextel.exception.DaoException;
import com.nextel.exception.UnhandledException;
import com.nextel.idao.INpProductDAO;
import com.nextel.utilities.ConnectionDB;
import com.nextel.utilities.Constant;
import com.nextel.utilities.StringUtils;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import oracle.jdbc.OracleCallableStatement;
import oracle.jdbc.driver.OracleTypes;

import org.apache.log4j.Logger;


/**
 * Data access object (DAO) for domain model class NpProduct.
 *
 * @see com.nextel.dao.NpProduct
 * @author Sonda del Perú
 */
public class NpProductDAO extends BaseDAO implements INpProductDAO {

    static Logger logger = Logger.getLogger(NpProductDAO.class);

    public void insertEntity(NpProduct bean)  {
        super.insertEntity(bean);
    }

    public void updateEntity(NpProduct bean)  {
        super.updateEntity(bean);
    }

    public void deleteEntity(NpProduct bean)   {
        super.deleteEntity(bean);
    }

    public NpProduct findById(java.lang.Long id)   {
        return ((NpProduct)super.findById(id));
    }

    public List getEntities(NpProduct beanParam)   {
        return (super.getEntities());
    }

    public List getEntityByProperty(String propertyName,
                                    Object value)  {
        return (super.getEntityByProperty(propertyName, value));
    }

    public String getInsertEntityStored() {
        return ("begin NpProduct_PKG.SP_INS_NpProduct( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?); end;");
    }

    public String getUpdateEntityStored() {
        return ("begin NpProduct_PKG.SP_UPD_NpProduct( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?); end;");
    }

    public String getDeleteEntityStored() {
        return ("begin NpProduct_PKG.SP_DEL_NpProduct(?,?); end;");
    }

    public String getFindByIdStored() {
        return ("begin NpProduct_PKG.SP_GET_NpProduct_DET(?,?,?); end;");
    }

    public String getAllEntitiesStored() {
        return ("begin NpProduct_PKG.SP_GET_NpProduct_LST(?,?); end;");
    }

    //CRM PARA TRAER LOS MODELOS(EQUIPOS) QUE SE VENDEN EN RETAIL --> TIPO DE LINEA "EQUIPO" -->PRE PAGO
// HHUARACA - PRY-0925

/*---------------------------------------------------------------------------------------------------------------------
  Purpose: Actions del tipo Consultas
  MODIFICATION HISTORY
  Person     Date         Comments
  ---------  ----------   ------------------------------------------------------------------------------------------
  HHUARACA   10/10/2017   PRY - 0925 Se agregó el parámetro para que considere el flag VEP */
  
     public List<NpProduct> lstProductxIdReatail(Long idPos,Long idRetail,Long idTypeLine,Long idSolution,Long idSubWarehouseAvailable,Long idLineaProducto,Integer cmbKitVep)  {
     
            Connection conn=null;
            String storedProcedure;
            CallableStatement stmt=null;
            String av_message;   
            
             ResultSet rs = null;
             av_message = null;
             storedProcedure = "begin NPPRODUCT_PKG.SP_GET_NpProductXPrePago_LST(?,?,?,?,?,?,?,?,?); end;";
             List<NpProduct> lstEntitiesxFilters = new ArrayList<NpProduct>();
             try{
                     conn = ConnectionDB.getConnection();
                     stmt = conn.prepareCall(storedProcedure);

                     //Se registra el parámetro de salida
                     stmt.registerOutParameter(1, OracleTypes.VARCHAR);

                     //Cursor de Salida
                      stmt.registerOutParameter(2, OracleTypes.CURSOR);

                     //Se configuran los demás parámetros del stored
					 //HHUARACA - PRY-0925 - Inicio
                     stmt = configureProduct2(stmt,idPos,idRetail,idTypeLine,idSolution,idSubWarehouseAvailable,idLineaProducto,cmbKitVep);
					 //HHUARACA - PRY-0925 - Fin	
                     //Se ejecuta el statement
                     stmt.execute();

                     //Se obtiene la respuesta error/exito del stored
                     av_message = (String)stmt.getObject(1);
                     boolean resultado=false;
                     resultado=this.handleErrorResult(av_message);

                     if(resultado){
                             //Se obtiene el objeto del cursor
                             rs = (ResultSet)stmt.getObject(2);

                             while(rs.next()){
                                     NpProduct modelProduct = setEntityAttributes2(rs);
                                     lstEntitiesxFilters.add(modelProduct);
                             }
                     }else{
                       logger.error("[NPProductDAO][Metodo: lstProductxIdReatail][SP: "+storedProcedure+"][MensajeError: "+av_message+"]");
                     }

                     
             }catch (Exception e){
                         throw  new  UnhandledException( "lstProductxIdReatail [ "+ Constant.PARAMETER_ERROREXCEPTION+" ] " + e.getMessage(),e);
             }finally{
                 //Se limpian las variables de conexión
                 ConnectionDB.close(conn, rs, stmt);
             }
             return(lstEntitiesxFilters);
    }

    //CRM PARA TRAER LOS MODELOS(EQUIPOS) QUE SE VENDEN EN RETAIL --> TIPO DE LINEA "EQUIPO" -->POST PAGO
/*---------------------------------------------------------------------------------------------------------------------
  Purpose: Actions del tipo Consultas
  MODIFICATION HISTORY
  Person     Date         Comments
  ---------  ----------   ------------------------------------------------------------------------------------------
  HHUARACA   10/10/2017   PRY - 0925 Se agregó el parámetro para que considere el flag VEP */

     public List<NpProduct> lstProductxPostPago(Long idPos,Long idRetail,Long idTypeLine,Long idSolution,Long idSubWarehouseAvailable,Long idLineaProducto,String strRiesgoFinan, String strIncompredictor,String tipoOperacion,Integer cmbKitVep)  {
     
            Connection conn=null;
            String storedProcedure;
            CallableStatement stmt=null;
            String av_message;   
            
             ResultSet rs = null;
             av_message = null;
             storedProcedure = "begin NPPRODUCT_PKG.SP_GET_NpProductXPostPago_LST(?,?,?,?,?,?,?,?,?,?,?,?); end;";
             List<NpProduct> lstEntitiesxFilters = new ArrayList<NpProduct>();
             try{
                     conn = ConnectionDB.getConnection();
                     stmt = conn.prepareCall(storedProcedure);

                     //Se registra el parámetro de salida
                     stmt.registerOutParameter(1, OracleTypes.VARCHAR);

                     //Cursor de Salida
                      stmt.registerOutParameter(2, OracleTypes.CURSOR);

                     //Se configuran los demás parámetros del stored
					 //HHUARACA - PRY-0925 - Inicio
                     stmt = configureProduct4(stmt,idPos,idRetail,idTypeLine,idSolution,idSubWarehouseAvailable,idLineaProducto,strRiesgoFinan, strIncompredictor,tipoOperacion,cmbKitVep);
					 //HHUARACA - PRY-0925 - Fin
                     //Se ejecuta el statement
                     stmt.execute();

                     //Se obtiene la respuesta error/exito del stored
                     av_message = (String)stmt.getObject(1);
                     boolean resultado=false;
                     resultado=this.handleErrorResult(av_message);

                     if(resultado){
                             //Se obtiene el objeto del cursor
                             rs = (ResultSet)stmt.getObject(2);

                             while(rs.next()){
                                     NpProduct modelProduct = setEntityAttributes2(rs);
                                     lstEntitiesxFilters.add(modelProduct);
                             }
                     }else{
                       logger.error("[NPProductDAO][Metodo: lstProductxPostPago][SP: "+storedProcedure+"][MensajeError: "+av_message+"]");
                     }

                     
             }catch (Exception e){
                 throw  new  UnhandledException("lstProductxPostPago [ "+ Constant.PARAMETER_ERRORSTORE +" ] " + av_message);
             }finally{
                 //Se limpian las variables de conexión
                 ConnectionDB.close(conn, rs, stmt);
             }
             return(lstEntitiesxFilters);
    }

    //CRM PARA TRAER LOS PRODUCTOS QUE NO SE ACTIVA ES DECIR BATERIAS... ETC TIPO DE LINEA "PRODUCTO"

     public List<ProductNotActivation> lstProductNotActivation(Long idPos,Long idRetail,Long idTypeLine,Long idSolution,Long idSubWarehouseAvailable,Long idLineaProducto)  {
             
            Connection conn=null;
            String storedProcedure;
            CallableStatement stmt=null;
            String av_message;   
            
             ResultSet rs = null;
             av_message = null;
             storedProcedure = "begin NPPRODUCT_PKG.SP_GET_NpProdNotActivation_LST(?,?,?,?,?,?,?,?); end;";
             List<ProductNotActivation> lstEntitiesxFilters = new ArrayList<ProductNotActivation>();
             try{
                     conn = ConnectionDB.getConnection();
                     stmt = conn.prepareCall(storedProcedure);

                     //Se registra el parámetro de salida
                     stmt.registerOutParameter(1, OracleTypes.VARCHAR);

                     //Cursor de Salida
                      stmt.registerOutParameter(2, OracleTypes.CURSOR);

                     //Se configuran los demás parámetros del stored
                      stmt.setLong(3, idPos);
                      stmt.setLong(4, idRetail);
                      stmt.setLong(5, idTypeLine);
                      stmt.setLong(6, idSolution);
                      stmt.setLong(7, idSubWarehouseAvailable);
                      stmt.setLong(8, idLineaProducto);
                     
                     //stmt = configureProduct2(stmt,idPos,idRetail,idTypeLine,idSolution,idSubWarehouseAvailable,idLineaProducto);

                     //Se ejecuta el statement
                     stmt.execute();

                     //Se obtiene la respuesta error/exito del stored
                     av_message = (String)stmt.getObject(1);
                     boolean resultado=false;
                     resultado=this.handleErrorResult(av_message);

                     if(resultado){
                             //Se obtiene el objeto del cursor
                             rs = (ResultSet)stmt.getObject(2);

                             while(rs.next()){
                                     ProductNotActivation product = setEntityAttributesProduct(rs);
                                     lstEntitiesxFilters.add(product);
                             }
                     }else{
                       logger.error("[NPProductDAO][Metodo: lstProductNotActivation][SP: "+storedProcedure+"][MensajeError: "+av_message+"]");
                     }

                     
             }catch (Exception e){
                     throw  new  UnhandledException( "lstProductNotActivation [ "+ Constant.PARAMETER_ERROREXCEPTION +" ] "+ e.getMessage(),e);
             }finally{
                 //Se limpian las variables de conexión
                 ConnectionDB.close(conn, rs, stmt);
             }
             return(lstEntitiesxFilters);
    }
    
    public List<ProductNotActivation> lstProductBySerie(Long idPos,Long idRetail,Long idTypeLine,Long idSolution,Long idSubWarehouseAvailable,Long idLineaProducto)  {
            
            Connection conn=null;
            String storedProcedure;
            CallableStatement stmt=null;
            String av_message;   
            
            ResultSet rs = null;
            av_message = null;
            storedProcedure = "begin NPPRODUCT_PKG.sp_get_npprodbyserie_lst(?,?,?,?,?,?,?,?); end;";
            List<ProductNotActivation> lstEntitiesxFilters = new ArrayList<ProductNotActivation>();
            try{
                    conn = ConnectionDB.getConnection();
                    stmt = conn.prepareCall(storedProcedure);

                    //Se registra el parámetro de salida
                    stmt.registerOutParameter(1, OracleTypes.VARCHAR);

                    //Cursor de Salida
                     stmt.registerOutParameter(2, OracleTypes.CURSOR);

                    //Se configuran los demás parámetros del stored
                     stmt.setLong(3, idPos);
                     stmt.setLong(4, idRetail);
                     stmt.setLong(5, idTypeLine);
                     stmt.setLong(6, idSolution);
                     stmt.setLong(7, idSubWarehouseAvailable);
                     stmt.setLong(8, idLineaProducto);
                    
                    //stmt = configureProduct2(stmt,idPos,idRetail,idTypeLine,idSolution,idSubWarehouseAvailable,idLineaProducto);

                    //Se ejecuta el statement
                    stmt.execute();

                    //Se obtiene la respuesta error/exito del stored
                    av_message = (String)stmt.getObject(1);
                    boolean resultado=false;
                    resultado=this.handleErrorResult(av_message);

                    if(resultado){
                            //Se obtiene el objeto del cursor
                            rs = (ResultSet)stmt.getObject(2);

                            while(rs.next()){
                                    ProductNotActivation product = setEntityAttributesProduct(rs);
                                    lstEntitiesxFilters.add(product);
                            }
                    }else{
                      logger.error("[NPProductDAO][Metodo: lstProductBySerie][SP: "+storedProcedure+"][MensajeError: "+av_message+"]");
                    }

                    
            }catch (Exception e){
                      throw  new  UnhandledException( "lstProductBySerie [ "+ Constant.PARAMETER_ERROREXCEPTION +" ] "+ e.getMessage(),e);
            }finally{
                //Se limpian las variables de conexión
                ConnectionDB.close(conn, rs, stmt);
            }
            return(lstEntitiesxFilters);
    }
    
    //CRM PARA TRAER LOS MODELOS QUE TIENE PRECIOS ESPECIALES
/*---------------------------------------------------------------------------------------------------------------------
  Purpose: Actions del tipo Consultas
  MODIFICATION HISTORY
  Person     Date         Comments
  ---------  ----------   ------------------------------------------------------------------------------------------
  HHUARACA   10/10/2017   PRY - 0925 Se agregó el parámetro para que considere el flag VEP */
  
     public List<NpProduct> lstProductSpecialPrice(Long idPos,Long idRetail,Long idTypeLine,Long idSolution,Long idSubWarehouseAvailable,Long idLineaProducto,Integer cmbKitVep)  {
             
             Connection conn=null;
             String storedProcedure;
             CallableStatement stmt=null;
             String av_message;   
             
             ResultSet rs = null;
             av_message = null;
             storedProcedure = "begin NPPRODUCT_PKG.SP_GET_NpProductSpecialPrice(?,?,?,?,?,?,?,?,?); end;";
             List<NpProduct> lstEntitiesxFilters = new ArrayList<NpProduct>();
             try{
                     conn = ConnectionDB.getConnection();
                     stmt = conn.prepareCall(storedProcedure);

                     //Se registra el parámetro de salida
                     stmt.registerOutParameter(1, OracleTypes.VARCHAR);

                     //Cursor de Salida
                      stmt.registerOutParameter(2, OracleTypes.CURSOR);

                     //Se configuran los demás parámetros del stored
					 //HHUARACA - PRY-0925 - Inicio
                     stmt = configureProduct(stmt,idPos,idRetail,idTypeLine,idSolution,idSubWarehouseAvailable,idLineaProducto,cmbKitVep);
					 //HHUARACA - PRY-0925 - Fin
                     //Se ejecuta el statement
                     stmt.execute();

                     //Se obtiene la respuesta error/exito del stored
                     av_message = (String)stmt.getObject(1);
                     boolean resultado=false;
                     resultado=this.handleErrorResult(av_message);

                     if(resultado){
                             //Se obtiene el objeto del cursor
                             rs = (ResultSet)stmt.getObject(2);

                             while(rs.next()){
                                     NpProduct modelProduct = setEntityAttributes2(rs);
                                     lstEntitiesxFilters.add(modelProduct);
                             }
                     }else{
                       logger.error("[NPProductDAO][Metodo: lstProductSpecialPrice][SP: "+storedProcedure+"][MensajeError: "+av_message+"]");  
                     }

                     
             }catch (Exception e){
                 throw  new  UnhandledException( "lstProductSpecialPrice [ "+ Constant.PARAMETER_ERROREXCEPTION +" ] "+ e.getMessage(),e);
             }finally{
                 //Se limpian las variables de conexión
                 ConnectionDB.close(conn, rs, stmt);
             }
             return(lstEntitiesxFilters);
     }

    //****

     public List<NpProduct> lstProductModelPostPago(Long idPos,Long idRetail,Long idTypeLine,Long idSolution,Long idSubWarehouseAvailable,Long idProductLine,Long idStore)    {
              
              Connection conn=null;
              String storedProcedure;
              CallableStatement stmt=null;
              String av_message;   
              
              ResultSet rs = null;
              av_message = null;
              storedProcedure = "begin NPPRODUCT_PKG.SP_GET_NpModelPostPago(?,?,?,?,?,?,?,?,?); end;";
              List<NpProduct> lstEntitiesxFilters = new ArrayList<NpProduct>();
              try{
                      conn = ConnectionDB.getConnection();
                      stmt = conn.prepareCall(storedProcedure);

                      //Se registra el parámetro de salida
                      stmt.registerOutParameter(1, OracleTypes.VARCHAR);

                      //Cursor de Salida
                       stmt.registerOutParameter(2, OracleTypes.CURSOR);

                      //Se configuran los demás parámetros del stored
                      stmt = configureProduct3(stmt,idPos,idRetail,idTypeLine,idSolution,idSubWarehouseAvailable,idProductLine,idStore);

                      //Se ejecuta el statement
                      stmt.execute();

                      //Se obtiene la respuesta error/exito del stored
                      av_message = (String)stmt.getObject(1);
                      boolean resultado=false;
                      resultado=this.handleErrorResult(av_message);

                      if(resultado){
                              //Se obtiene el objeto del cursor
                              rs = (ResultSet)stmt.getObject(2);

                              while(rs.next()){
                                      NpProduct modelProduct = setEntityAttributes3(rs);
                                      lstEntitiesxFilters.add(modelProduct);
                              }
                       }else{
                         logger.error("[NPProductDAO][Metodo: lstProductModelPostPago][SP: "+storedProcedure+"][MensajeError: "+av_message+"]");
                      }

                      
              }catch (Exception e){
                       throw  new  UnhandledException( "lstProductModelPostPago [ "+ Constant.PARAMETER_ERROREXCEPTION +" ] "+ e.getMessage(),e);
              }finally{
                  //Se limpian las variables de conexión
                  ConnectionDB.close(conn, rs, stmt);
              }
              return(lstEntitiesxFilters);

          }

     //CRM PRODUCTOS QUE SE PUEDE ASIGANR SKU
     //LEL 06-01-10: Nuevo parametro de entrada que se saca del Bean
      public List<Object> getProductSKU(NpProduct beanParam)  {
              
              Connection conn=null;
              String storedProcedure;
              CallableStatement stmt=null;
              String av_message;   
              
              ResultSet rs = null;
              av_message = null;
              storedProcedure = "begin NpProduct_PKG.SP_GET_NpProductSKU_LST(?,?,?); end;";
              List<Object> lstAllEntities = new ArrayList<Object>();
              try{
                      conn = ConnectionDB.getConnection();
                      stmt = conn.prepareCall(storedProcedure);

                      //Se registra el parámetro de salida
                      stmt.registerOutParameter(2, OracleTypes.VARCHAR);
                      stmt.registerOutParameter(3, OracleTypes.CURSOR);
                      //Se configuran los demás parámetros del stored
                      stmt = configureProductSKU(stmt,beanParam.getNpproductid());

                      //Se ejecuta el statement
                      stmt.execute();

                      //Se obtiene la respuesta error/exito del stored
                      av_message = (String)stmt.getObject(2);
                      boolean resultado=false;
                      resultado=this.handleErrorResult(av_message);

                      if(resultado){
                              //Se obtiene el objeto del cursor
                              rs = (ResultSet)stmt.getObject(3);

                              while(rs.next()){
                                      Object npAddress = setEntityAttributes(rs);
                                      lstAllEntities.add(npAddress);
                              }
                      }else{
                        logger.error("[NPProductDAO][Metodo: getProductSKU][SP: "+storedProcedure+"][MensajeError: "+av_message+"]");
                      }

                      
              }catch (Exception e){
                  throw  new  UnhandledException( "getProductSKU [ "+ Constant.PARAMETER_ERROREXCEPTION +" ] "+ e.getMessage(),e);
              }finally{
                  //Se limpian las variables de conexión
                  ConnectionDB.close(conn, rs, stmt);
              }
              return(lstAllEntities);
      }

    //JTORRESC 20-12-2010: obtener la lista de kit's por cadena
     public List<Object> getKitxCadena(Long idRetailer)  {
             
             Connection conn=null;
             String storedProcedure;
             CallableStatement stmt=null;
             String av_message;   
             
             ResultSet rs = null;
             av_message = null;
             storedProcedure = "begin NpProduct_PKG.sp_get_kitxretailer_lst(?,?,?); end;";
             List<Object> lstAllEntities = new ArrayList<Object>();
             try{
                     conn = ConnectionDB.getConnection();
                     stmt = conn.prepareCall(storedProcedure);

                     //Se registra el parámetro de salida                     
                     stmt.registerOutParameter(2, OracleTypes.CURSOR);
                     stmt.registerOutParameter(3, OracleTypes.VARCHAR);
                     //Se configuran los demás parámetros del stored
                     stmt = configureProductSKU(stmt, idRetailer);

                     //Se ejecuta el statement
                     stmt.execute();

                     //Se obtiene la respuesta error/exito del stored
                     av_message = (String)stmt.getObject(3);
                     boolean resultado=false;
                     resultado=this.handleErrorResult(av_message);

                     if(resultado){
                             //Se obtiene el objeto del cursor
                             rs = (ResultSet)stmt.getObject(2);

                             while(rs.next()){
                                     Object npAddress = setEntityAttributesKitxCadena(rs);
                                     lstAllEntities.add(npAddress);
                             }
                     }else{
                       logger.error("[NPProductDAO][Metodo: getProductSKU][SP: "+storedProcedure+"][MensajeError: "+av_message+"]" +
                                    "[Parametros][idRetailer:"+idRetailer+"]");
                     }

             }catch (Exception e){
                 throw  new  UnhandledException( "getKitxCadena [ "+ Constant.PARAMETER_ERROREXCEPTION +" ] "+ e.getMessage(),e);
             }finally{
                 //Se limpian las variables de conexión
                 ConnectionDB.close(conn, rs, stmt);
             }
             return(lstAllEntities);
     }
    
    //JTORRESC 20-12-2010: obtener la lista de kit's por cadena
     public List<Object> getRepoKitxCadena(Long idRetailer, Long idProduct)  {
             
             Connection conn=null;
             String storedProcedure;
             CallableStatement stmt=null;
             String av_message;   
             
             ResultSet rs = null;
             av_message = null;
             storedProcedure = "begin NpProduct_PKG.sp_get_repokitxretailer_lst(?,?,?,?); end;";
             List<Object> lstAllEntities = new ArrayList<Object>();
             try{
                     conn = ConnectionDB.getConnection();
                     stmt = conn.prepareCall(storedProcedure);

                     //Se registra el parámetro de salida                     
                     stmt.registerOutParameter(3, OracleTypes.CURSOR);
                     stmt.registerOutParameter(4, OracleTypes.VARCHAR);
                     //Se configuran los demás parámetros del stored
                     stmt = configureRepoKitxCadena(stmt, idRetailer, idProduct);

                     //Se ejecuta el statement
                     stmt.execute();

                     //Se obtiene la respuesta error/exito del stored
                     av_message = (String)stmt.getObject(4);
                     boolean resultado=false;
                     resultado=this.handleErrorResult(av_message);
                    

                     if(resultado){
                             //Se obtiene el objeto del cursor
                             rs = (ResultSet)stmt.getObject(3);

                             while(rs.next()){
                                     Object npAddress = setEntityAttributesRepoKitxCadena(rs);
                                     lstAllEntities.add(npAddress);
                             }
                     }else{
                       logger.error("[NPProductDAO][Metodo: getRepoKitxCadena][SP: "+storedProcedure+"][MensajeError: "+av_message+"]" +
                                    "[Parametros][idRetailer:"+idRetailer+"][idProduct:"+idProduct+"]");
                     }

             }catch (Exception e){
                         throw  new  UnhandledException( "getRepoKitxCadena [ "+ Constant.PARAMETER_ERROREXCEPTION +" ] "+ e.getMessage(),e);
             }finally{
                 //Se limpian las variables de conexión
                 ConnectionDB.close(conn, rs, stmt);
             }
             return(lstAllEntities);
     }
     
    private CallableStatement configureProductSKU(CallableStatement stmt, Long idModelo)
    throws SQLException{
            stmt.setLong(1, idModelo);
            return(stmt);
    }
    
    private CallableStatement configureRepoKitxCadena(CallableStatement stmt, Long idRetailer, Long idProduct)
    throws SQLException{
            stmt.setLong(1, idRetailer);
            stmt.setLong(2, idProduct);
            return(stmt);
    }   
     //***

    public Long getId(Object obj) {
        NpProduct bean = (NpProduct)obj;
        if (bean.getNpproductid() != null) {
            return (bean.getNpproductid());
        } else {
            return (null);
        }
    }

    public CallableStatement configureParametersInsertUpdate(Object obj,
                                                             CallableStatement stmt) throws SQLException {
        NpProduct bean = (NpProduct)obj;

        if (bean.getNpproductid() != null) {
            stmt.setLong(2, bean.getNpproductid());
        } else {
            stmt.setNull(2, OracleTypes.NUMBER);
        }

        if (bean.getNpplanid() != null) {
            stmt.setLong(3, bean.getNpplanid());
        } else {
            stmt.setNull(3, OracleTypes.NUMBER);
        }
        if (bean.getNpproductstatus() != null) {
            stmt.setLong(4, bean.getNpproductstatus());
        } else {
            stmt.setNull(4, OracleTypes.NUMBER);
        }
        stmt.setString(5, bean.getNpnextelcode());
        stmt.setString(6, bean.getNpproductname());
        stmt.setFloat(7, bean.getNpcost());
        if (bean.getNpcurrency() != null) {
            stmt.setLong(8, bean.getNpcurrency());
        } else {
            stmt.setNull(8, OracleTypes.NUMBER);
        }
        stmt.setString(9, bean.getNpkit());
        stmt.setString(10, bean.getNpcreatedby());
        stmt.setString(11, bean.getNpmodifiedby());
        stmt.setString(12, bean.getNpstatus());
        return (stmt);
    }



    public Object setEntityAttributes(ResultSet rs) throws SQLException {
        NpProduct bean = new NpProduct();
        bean.setNpproductid(new Long(rs.getLong("NPPRODUCTID")));
        bean.setNpplanid(new Long(rs.getLong("Npplanid")));
        bean.setNpproductstatus(new Long(rs.getLong("Npproductstatus")));
        bean.setNpnextelcode(rs.getString("Npnextelcode"));
        bean.setNpproductname(rs.getString("Npproductname"));
        bean.setNpcost(rs.getFloat("Npcost"));
        bean.setNpcurrency(new Long(rs.getLong("Npcurrency")));
        bean.setNpkit(rs.getString("Npkit"));
        bean.setNpcreatedby(rs.getString("Npcreatedby"));
        bean.setNpmodifiedby(rs.getString("Npmodifiedby"));
        bean.setNpstatus(rs.getString("Npstatus"));
        return (bean);
    }
    
    public Object setEntityAttributesKitxCadena(ResultSet rs) throws SQLException {
        NpProduct bean = new NpProduct();
        bean.setNpproductid(new Long(rs.getLong("NPPRODUCTID")));        
        bean.setNpproductname(rs.getString("Npproductname"));        
        return (bean);
    }
    
    public Object setEntityAttributesRepoKitxCadena(ResultSet rs) throws SQLException {
        NpProduct bean = new NpProduct();
        bean.setNpproductid(new Long(rs.getLong("NPPRODUCTID")));        
        bean.setNpproductname(rs.getString("Npproductname"));
        bean.setSkuProduct(rs.getString("npsku"));
        bean.setNpcost(rs.getFloat("price"));
        bean.setCurrencyDesc(rs.getString("currency"));
        return (bean);
    }
    
    //CRM

     public ProductNotActivation setEntityAttributesProduct(ResultSet rs) throws SQLException {
         ProductNotActivation bean = new ProductNotActivation();
         bean.setIdProduct(rs.getLong("NPPRODUCTID"));
         bean.setNameProduct(rs.getString("NPPRODUCTNAME"));
         bean.setIdProductLine(rs.getLong("NPPRODUCTLINEID"));
         bean.setSkuProduct(rs.getString("NPSKU"));
         //bean.setCostProduct(rs.getDouble("NPCOST"));
         //BALFARO 3-12-2010 INICIO
         bean.setCostProduct(rs.getDouble("NPPRICE"));
         //BALFARO 3-12-2010 FIN
         bean.setCurrencyProduct(rs.getLong("NPCURRENCY"));
         //bean.setNumberProduct(rs.getString("NPIMEI"));
         return (bean);
     }
	 
  /*---------------------------------------------------------------------------------------------------------------------
  Purpose: Actions del tipo Consultas
  MODIFICATION HISTORY
  Person     Date         Comments
  ---------  ----------   ------------------------------------------------------------------------------------------
  HHUARACA   10/10/2017   PRY - 0925 Se agregó el parámetro para que considere el flag VEP */
  
     private CallableStatement configureProduct(CallableStatement stmt,Long idPos,Long idRetail,Long idTypeLine,Long idSolution,Long idSubWarehouseAvailable,Long idLineaProducto,Integer cmbKitVep)
     throws SQLException{
        int param_position1 = 3;
        int param_position2 = 4;
        int param_position3 = 5;
        int param_position4 = 6;
        int param_position5 = 7;
        int param_position6 = 8;
		//HHUARACA - PRY-0925 - Inicio
        int param_position7=9;
		//HHUARACA - PRY-0925 - Fin
        
        stmt.setLong(param_position1++, idPos);
        stmt.setLong(param_position2++, idRetail);
        stmt.setLong(param_position3++, idTypeLine);
        stmt.setLong(param_position4++, idSolution);
        stmt.setLong(param_position5++, idSubWarehouseAvailable);
        stmt.setLong(param_position6++, idLineaProducto);
		//HHUARACA - PRY-0925 - Inicio
        stmt.setInt(param_position7++, cmbKitVep);
		//HHUARACA - PRY-0925 - Fin
        
        return(stmt);
     }

  /*---------------------------------------------------------------------------------------------------------------------
  Purpose: Actions del tipo Consultas
  MODIFICATION HISTORY
  Person     Date         Comments
  ---------  ----------   ------------------------------------------------------------------------------------------
  HHUARACA   10/10/2017   PRY - 0925 Se agregó el parámetro para que considere el flag VEP */
  
    private CallableStatement configureProduct2(CallableStatement stmt,Long idPos,Long idRetail,Long idTypeLine,Long idSolution,Long idSubWarehouseAvailable,Long idLineaProducto,Integer cmbKitVep)
    throws SQLException{
       int param_position1 = 3;
       int param_position2 = 4;
       int param_position3 = 5;
       int param_position4 = 6;
       int param_position5 = 7;
       int param_position6 = 8;
	   //HHUARACA - PRY-0925 - Inicio
       int param_position7=9;
       //HHUARACA - PRY-0925 - Fin
	   
       stmt.setLong(param_position1++, idPos);
       stmt.setLong(param_position2++, idRetail);
       stmt.setLong(param_position3++, idTypeLine);
       stmt.setLong(param_position4++, idSolution);
       stmt.setLong(param_position5++, idSubWarehouseAvailable);
       stmt.setLong(param_position6++, idLineaProducto);
	   //HHUARACA - PRY-0925 - Inicio
        stmt.setInt(param_position7++, cmbKitVep);
	   //HHUARACA - PRY-0925 - Fin

       return(stmt);
    }

  /*---------------------------------------------------------------------------------------------------------------------
  Purpose: Actions del tipo Consultas
  MODIFICATION HISTORY
  Person     Date         Comments
  ---------  ----------   ------------------------------------------------------------------------------------------
  HHUARACA   10/10/2017   PRY - 0925 Se agregó el parámetro para que considere el flag VEP */
  
    private CallableStatement configureProduct4(CallableStatement stmt,Long idPos,Long idRetail,Long idTypeLine,Long idSolution,Long idSubWarehouseAvailable,Long idLineaProducto,String strRiesgoFinan,String strIncompredictor,String tipoOperacion,Integer cmbKitVep)
    throws SQLException{
       int param_position1 = 3;
       int param_position2 = 4;
       int param_position3 = 5;
       int param_position4 = 6;
       int param_position5 = 7;
       int param_position6 = 8;
       int param_position7 = 9;
       int param_position8 = 10;
       int param_position9 = 11;
	   //HHUARACA - PRY-0925 - Inicio
        int param_position10=12;
		//HHUARACA - PRY-0925 - Fin

       
       stmt.setLong(param_position1++, idPos);
       stmt.setLong(param_position2++, idRetail);
       stmt.setLong(param_position3++, idTypeLine);
       stmt.setLong(param_position4++, idSolution);
       stmt.setLong(param_position5++, idSubWarehouseAvailable);
       stmt.setLong(param_position6++, idLineaProducto);
       stmt.setString(param_position7++, strRiesgoFinan);
       stmt.setString(param_position8++, strIncompredictor);
       stmt.setString(param_position9++, tipoOperacion);
	   //HHUARACA - PRY-0925 - Inicio
        stmt.setInt(param_position10++, cmbKitVep);
		//HHUARACA - PRY-0925 - Fin

        
       return(stmt);
    }
    /*private CallableStatement configureProduct3(CallableStatement stmt,Long idPos,Long idRetail,Long idTypeLine,Long idSolution,Long idSubWarehouseAvailable)
    throws SQLException{
       int param_position1 = 3;
       int param_position2 = 4;
       int param_position3 = 5;
       int param_position4 = 6;
       int param_position5 = 7;
       //int param_position6 = 8;
       stmt.setLong(param_position1++, idPos);
       stmt.setLong(param_position2++, idRetail);
       stmt.setLong(param_position3++, idTypeLine);
       stmt.setLong(param_position4++, idSolution);
       stmt.setLong(param_position5++, idSubWarehouseAvailable);
       //stmt.setLong(param_position6++, idLineaProducto);
       return(stmt);
    }*/

    public NpProduct setEntityAttributes2(ResultSet rs) throws SQLException {
        NpProduct bean = new NpProduct();

        bean.setNpproductid(new Long(rs.getLong("NPPRODUCTID")));
        bean.setNpproductname(rs.getString("Npproductname"));
        bean.setNpproductlineid(rs.getLong("NPPRODUCTLINEID"));
        return (bean);
    }
    //*

     private CallableStatement configureProduct3(CallableStatement stmt,Long idPos,Long idRetail,Long idTypeLine,Long idSolution,Long idSubWarehouseAvailable, Long idProductLine,Long idStore)
     throws SQLException{
        int param_position1 = 3;
        int param_position2 = 4;
        int param_position3 = 5;
        int param_position4 = 6;
        int param_position5 = 7;
        int param_position6 = 8;
        int param_position7 = 9;
        stmt.setLong(param_position1++, idPos);
        stmt.setLong(param_position2++, idRetail);
        stmt.setLong(param_position3++, idTypeLine);
        stmt.setLong(param_position4++, idSolution);
        stmt.setLong(param_position5++, idSubWarehouseAvailable);
        stmt.setLong(param_position6++, idProductLine);
        stmt.setLong(param_position7++, idStore);
        return(stmt);
     }

     //EGH
         public NpProduct setEntityAttributes3(ResultSet rs) throws SQLException {
             NpProduct bean = new NpProduct();

             bean.setNpproductid(new Long(rs.getLong("NPPRODUCTID")));
             bean.setNpproductname(rs.getString("Npproductname"));
             bean.setNpproductlineid(rs.getLong("NPPRODUCTLINEID"));
             bean.setNpcost(rs.getFloat("NPCOST"));
             bean.setSkuProduct(rs.getString("NPSKU"));
             return (bean);
         }
        //

  /*---------------------------------------------------------------------------------------------------------------------
  Purpose: Actions del tipo Consultas
  MODIFICATION HISTORY
  Person     Date         Comments
  ---------  ----------   ------------------------------------------------------------------------------------------
  HHUARACA   10/10/2017   PRY - 0925 Se agregó el parámetro para que considere el flag VEP */
  
         public List<NpProduct> lstProductxPreVenta(Long idPos,Long idRetail,Long idTypeLine,Long idSolution,Long idSubWarehouseAvailable,Long idLineaProducto,Integer cmbKitVep) {
                     
                     Connection conn=null;
                     String storedProcedure;
                     CallableStatement stmt=null;
                     String av_message;   
                     
                     ResultSet rs = null;
                     av_message = null;
                     storedProcedure = "begin NPPRODUCT_PKG.SP_GET_NpProductXPreVenta_LST(?,?,?,?,?,?,?,?,?); end;";
                     List<NpProduct> lstEntitiesxFilters = new ArrayList<NpProduct>();
                     try{
                             conn = ConnectionDB.getConnection();
                             stmt = conn.prepareCall(storedProcedure);

                             //Se registra el parámetro de salida
                             stmt.registerOutParameter(1, OracleTypes.VARCHAR);

                             //Cursor de Salida
                              stmt.registerOutParameter(2, OracleTypes.CURSOR);

                             //Se configuran los demás parámetros del stored
							 //HHUARACA - PRY-0925 - Inicio
                             stmt = configureProduct2(stmt,idPos,idRetail,idTypeLine,idSolution,idSubWarehouseAvailable,idLineaProducto,cmbKitVep);
							 //HHUARACA - PRY-0925 - Fin
                             //Se ejecuta el statement
                             stmt.execute();

                             //Se obtiene la respuesta error/exito del stored
                             av_message = (String)stmt.getObject(1);
                             boolean resultado=false;
                             resultado=this.handleErrorResult(av_message);
                             

                             if(resultado){
                                     //Se obtiene el objeto del cursor
                                     rs = (ResultSet)stmt.getObject(2);

                                     while(rs.next()){
                                             NpProduct modelProduct = setEntityAttributes2(rs);
                                             lstEntitiesxFilters.add(modelProduct);
                                     }
                             }else{
                               logger.error("[NPProductDAO][Metodo: lstProductxPreVenta][SP: "+storedProcedure+"][MensajeError: "+av_message+"]");
                             }

                     }catch (Exception e){
                         throw  new  UnhandledException( "lstProductxPreVenta  [ "+ Constant.PARAMETER_ERROREXCEPTION +" ] "+ e.getMessage(),e);
                     }finally{
                         //Se limpian las variables de conexión
                         ConnectionDB.close(conn, rs, stmt);
                     }
                     return(lstEntitiesxFilters);
             }


   /**
     *   Method : getAvailableProducts
     *   Purpose:
     *   Developer       Fecha       Comentario
     *   =============   ==========  ======================
     *   Miguel Jurado   02/02/2010  Creación
     *   JTORRESC        19/07/2010  Se cambio los pararametros
     **/
    public List<NpAvailableProduct> getAvailableProducts(NpAvailableProduct bean)  {        
    
        Connection conn=null;
        String storedProcedure;
        CallableStatement stmt=null;
        String av_message;   
        
        NpAvailableProduct availableProduct;
        OracleCallableStatement cstmt = null;
        
        String strMessage = null;
        ResultSet rs = null;
        ArrayList<NpAvailableProduct> lstDetail = null;        
        ArrayList<NpAvailableProduct> lstAvailableDetail = null;        
        storedProcedure = "begin RETAIL.NPPRODUCT_PKG.sp_get_availableproducts_lst(?,?,?,?,?,?,?,?); end;";
        try {
            conn = ConnectionDB.getConnection();
            cstmt = (OracleCallableStatement)conn.prepareCall(storedProcedure);
            cstmt.setLong(1, bean.getNpretailerid());
            cstmt.setLong(2, bean.getNpstoreid());
            cstmt.setLong(3, bean.getNpposid());
            cstmt.setLong(4, bean.getNpproductid());
            cstmt.setLong(5, bean.getNpsubwarehouseid());
            cstmt.registerOutParameter(6, OracleTypes.CURSOR);
            cstmt.registerOutParameter(7, OracleTypes.CURSOR);            
            cstmt.registerOutParameter(8, Types.VARCHAR);
            cstmt.execute();
            strMessage = cstmt.getString(8);
            boolean resultado=false;
            resultado=this.handleErrorResult(strMessage);
            
            
            if (resultado) {
                //Se obtiene el objeto del cursor de detalle
                rs = (ResultSet)cstmt.getObject(7);
                lstAvailableDetail = new ArrayList<NpAvailableProduct>();
                lstDetail = new ArrayList<NpAvailableProduct>();
                while(rs.next()){                    
                    availableProduct = new NpAvailableProduct();                   
                    availableProduct.setNpretailerid(new Long(rs.getString("npretailerid")));
                    availableProduct.setNpstoreid(new Long(rs.getString("npstoreid")));
                    availableProduct.setNpposid(new Long(rs.getString("npposid")));
                    availableProduct.setNpproductid(new Long(rs.getString("npproductid")));
                    availableProduct.setNpsubwarehouseType(new Long(rs.getString("npsubwarehousetype")));
                    
                    if(bean.getStatusAvailableId().equals(availableProduct.getNpsubwarehouseType())){
                        availableProduct.setTotalAvailable(new Long(rs.getString("cantidad")));                        
                    }else if(bean.getStatusReturnedId().equals(availableProduct.getNpsubwarehouseType())){
                        availableProduct.setTotalReturned(new Long(rs.getString("cantidad")));
                    }
                    lstAvailableDetail.add(availableProduct);                  
                }
                
                //Se obtiene el objeto del cursor Principal
                rs = (ResultSet)cstmt.getObject(6);
                while(rs.next()){
                    availableProduct = new NpAvailableProduct();
                    for(NpAvailableProduct temp : lstAvailableDetail){                        
                        if((temp.getNpretailerid().toString()+""+temp.getNpstoreid().toString()+""+temp.getNpposid().toString()+""+temp.getNpproductid().toString()).equals(
                           (rs.getString("npretailerid").toString()+""+rs.getString("npstoreid").toString()+""+rs.getString("npposid").toString()+""+rs.getString("npproductid").toString()))
                          ){                          
                          if (bean.getStatusAvailableId().equals(temp.getNpsubwarehouseType())){
                            availableProduct.setTotalAvailable(temp.getTotalAvailable());
                          }else if (bean.getStatusReturnedId().equals(temp.getNpsubwarehouseType())){
                            availableProduct.setTotalReturned(temp.getTotalReturned());
                          }
                        }
                    }
                    availableProduct.setTotalAvailable(availableProduct.getTotalAvailable()==null? 0L:availableProduct.getTotalAvailable());
                    availableProduct.setTotalReturned(availableProduct.getTotalReturned()==null ? 0L :availableProduct.getTotalReturned());
                    availableProduct.setNpproductmodel(rs.getString("npproductname"));
                    availableProduct.setRetailername(rs.getString("retailername")); 
                    availableProduct.setStorename(rs.getString("storename"));
                    availableProduct.setPosname(rs.getString("posname"));                    
                    lstDetail.add(availableProduct);
                }
            }else{
              logger.error("[NPProductDAO][Metodo: getAvailableProducts][SP: "+storedProcedure+"][MensajeError: "+strMessage+"]");
            }
            
            
        } catch(SQLException sq){
            throw  new  UnhandledException( "getAvailableProducts [ "+ Constant.PARAMETER_ERROREXCEPTION +" ] "+ sq.getMessage(),sq);
        }catch(Exception e){
            throw  new  UnhandledException( "getAvailableProducts [ "+ Constant.PARAMETER_ERROREXCEPTION +" ] "+ e.getMessage(),e);
        }finally{
            ConnectionDB.closeSTMT(conn, cstmt, rs);
            lstAvailableDetail = null;            
        }
        return lstDetail;
    }

    public List<NpSubWarehouseDetail> getProductStockDetail(NpAvailableProduct bean)  {                
    
        Connection conn=null;
        String storedProcedure;
        CallableStatement stmt=null;
        String av_message;   
        
        OracleCallableStatement cstmt = null;
        
        String strMessage;
        ResultSet rs = null;
        ArrayList<NpSubWarehouseDetail> lstDetail = null;
        
        storedProcedure = "begin RETAIL.NPPRODUCT_PKG.sp_get_product_stock_detail(?,?,?,?,?,?,?,?,?); end;";
        try {
            conn = ConnectionDB.getConnection();
            cstmt = (OracleCallableStatement)conn.prepareCall(storedProcedure);
            cstmt.setLong(1, bean.getNpretailerid());
            cstmt.setLong(2, bean.getNpstoreid());
            cstmt.setLong(3, bean.getNpposid());
            cstmt.setLong(4, bean.getNpproductid());
            cstmt.setLong(5, bean.getNpsubwarehouseid());
            cstmt.setLong(6, bean.getNpsubwarehouseType());
            cstmt.setString(7, bean.getNpstatus());            
            cstmt.registerOutParameter(8, OracleTypes.CURSOR);
            cstmt.registerOutParameter(9, Types.VARCHAR);
            cstmt.execute();
            strMessage = cstmt.getString(9);
            boolean resultado;
            resultado=this.handleErrorResult(strMessage);
            
            if (resultado) {
                lstDetail = new ArrayList<NpSubWarehouseDetail>();                
                rs = (ResultSet)cstmt.getObject(8);
                while (rs.next()) {
                    NpSubWarehouseDetail detail = new NpSubWarehouseDetail();
                    
                    detail.setNpretailername((String)rs.getString("ret_name")+"");
                    detail.setNpstorename((String)rs.getString("sto_name")+"");
                    detail.setNpposname((String)rs.getString("pos_name")+"");
                    detail.setNpSubWarehouseType((String)rs.getString("tipo_almacen")+"");
                    detail.setNpProduct(new NpProduct((String)rs.getString("product_name")+""));
                    detail.setNpimei((String)rs.getString("imei")+"");
                    detail.setNpsim((String)rs.getString("sim")+"");                        
                    detail.setNpstatus((String)rs.getString("status_stock")+"");
                    
                    lstDetail.add(detail);
                }               
                
            }else{
              logger.error("[NPProductDAO][Metodo: getProductStockDetail][SP: "+storedProcedure+"][MensajeError: "+strMessage+"]");
            }
        } catch(SQLException sq){
            throw  new  UnhandledException( "getProductStockDetail [ "+ Constant.PARAMETER_ERROREXCEPTION +" ] "+ sq.getMessage(),sq);
        }catch(Exception e){
            throw  new  UnhandledException( "getProductStockDetail [ "+ Constant.PARAMETER_ERROREXCEPTION +" ] "+ e.getMessage(),e);
        }finally{
            ConnectionDB.closeSTMT(conn, cstmt, rs);
        }
        return lstDetail;
    }

    public List<NpSubWarehouseDetail> getTransferReturn(NpAvailableProduct bean)   {                
        
        Connection conn=null;
        String storedProcedure;
        CallableStatement stmt=null;
        String av_message;   
        
        OracleCallableStatement cstmt = null;
        
        String strMessage;
        ResultSet rs = null;
        ArrayList<NpSubWarehouseDetail> lstDetail = null;
        
        storedProcedure = "begin RETAIL.NPPRODUCT_PKG.sp_get_transfer_return(?,?,?,?,?,?); end;";
        try {
            conn = ConnectionDB.getConnection();
            cstmt = (OracleCallableStatement)conn.prepareCall(storedProcedure);
            cstmt.setLong(1, bean.getNpretailerid());
            cstmt.setLong(2, bean.getNpstoreid());
            cstmt.setLong(3, bean.getNpposid());
            cstmt.setLong(4, bean.getNpproductid());            
            cstmt.registerOutParameter(5, OracleTypes.CURSOR);
            cstmt.registerOutParameter(6, Types.VARCHAR);
            cstmt.execute();
            strMessage = cstmt.getString(6);
            boolean resultado=false;
            resultado=this.handleErrorResult(strMessage);
            if (!resultado) {
               throw  new  UnhandledException("getTransferReturn [ "+ Constant.PARAMETER_ERRORSTORE +" ] " + strMessage);
            } else {
                lstDetail = new ArrayList<NpSubWarehouseDetail>();                
                rs = (ResultSet)cstmt.getObject(5);
                while (rs.next()) {
                    NpSubWarehouseDetail detail = new NpSubWarehouseDetail();                    
                     detail.setNpsubwarehousedetailid((Long)rs.getLong("npsubwarehousedetailid"));
                    detail.setNpProduct(new NpProduct((String)rs.getString("product_name")+""));
                    detail.setNpimei((String)rs.getString("imei")+"");                    
                    lstDetail.add(detail);
                }               
            }//Fin Else
                
            
        } catch(SQLException sq){
            throw  new  UnhandledException( "getTransferReturn [ "+ Constant.PARAMETER_ERROREXCEPTION +" ] "+ sq.getMessage(),sq);
        }catch(Exception e){
            throw  new  UnhandledException( "getTransferReturn [ "+ Constant.PARAMETER_ERROREXCEPTION +" ] "+ e.getMessage(),e);
        }finally{
            ConnectionDB.closeSTMT(conn, cstmt, rs);
        }
        return lstDetail;
    }
    
    public String getProcessTransferReturn(String cadena, Long idPos, Long subwarehouseAvailable, Long subwarehouseRefund, String modifiedby)  {
        
        Connection conn=null;
        String storedProcedure;
        CallableStatement stmt=null;
        String av_message;   
        
        OracleCallableStatement cstmt = null;
        
        String strMessage ="";
        ResultSet rs = null;
        ArrayList<NpSubWarehouseDetail> lstDetail = null;
        
        storedProcedure = "begin RETAIL.NPPRODUCT_TRANSFER_PKG.sp_upd_transferavalreturn(?,?,?,?,?,?); end;";
        try {
            conn = ConnectionDB.getConnection();
            cstmt = (OracleCallableStatement)conn.prepareCall(storedProcedure);
            cstmt.setString(1, cadena);
            cstmt.setLong(2, idPos);
            cstmt.setLong(3, subwarehouseAvailable);
            cstmt.setLong(4, subwarehouseRefund);
            cstmt.setString(5, modifiedby);
            cstmt.registerOutParameter(6, Types.VARCHAR);
            cstmt.executeUpdate();
            strMessage = cstmt.getString(6);
            boolean resultado=false;
            resultado=this.handleErrorResult(strMessage);
            
         if (!resultado) {
              logger.error("[NPProductDAO][Metodo: getProcessTransferReturn][SP: "+storedProcedure+"][MensajeError: "+strMessage+"]"); 
            } 
            
        } catch(SQLException sq){
            throw  new  UnhandledException( "getProcessTransferReturn [ "+ Constant.PARAMETER_ERROREXCEPTION +" ] "+ sq.getMessage(),sq);
        }catch(Exception e){
            throw  new  UnhandledException( "getProcessTransferReturn [ "+ Constant.PARAMETER_ERROREXCEPTION +" ] "+ e.getMessage(),e);
        }finally{
            ConnectionDB.closeSTMT(conn, cstmt, rs);
        }
        return strMessage;
    }
    //******************
        // MODIFICATION HISTORY
        // Person          Date         Comments
        // ------------    ----------   -------------------------------------------
        //KCARPIOT         17-07-2014   RENIEC_IDDATA10: Se modifico reporte para agregar columnas de display:
        //                              nporderhour, npdocumenttype, npverificacion, npconexion
        //                              en BD : HORA, TIPDOCNUMBER, VERIFICACION,CONEXION
        // KCARPIOT        31-07-2014   PORTA: Se agrega campos de Descripcion
        //                              en BD:  ORIGENPORTA, ESTADOPORTA, CEDENTEPORTA
        // KCARPIOT        12-08-2014   DEPOSITO EN GARANTIA: Se agrega campo de Descripcion del 
        //                              Tipo de Depósito para Reporte de Liquidaciones
        //VPEREZ             20/09/2017  VEP : SE AGREGA CAMPO DE CAMPO INICIAL, MONTO FINANCIAR Y NRO COUTA
         
         public List<NpLiquidation> getLiquidationReport(Map bean)  {                
         
             Connection conn=null;
             String storedProcedure;
             CallableStatement stmt=null;
             String av_message;   
             
             OracleCallableStatement cstmt = null;
             
             String strMessage;
             ResultSet rs = null;
             ArrayList<NpLiquidation> lstLiquidation = null;
             
            //storedProcedure = "BEGIN RETAIL.NP_REPORT_PKG.SP_GET_LIQUIDACION2(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?); END;";

            storedProcedure = 
            "BEGIN RETAIL.NP_REPORT_PKG.SP_GET_LIQUIDACION(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?); END;";
             
             try {
                 conn = ConnectionDB.getConnection();
                 
                 cstmt = (OracleCallableStatement) conn.prepareCall(storedProcedure,
                                                                         ResultSet.TYPE_SCROLL_INSENSITIVE, 
                                                                         ResultSet.CONCUR_READ_ONLY);
                 cstmt.setFetchSize(100);                                                                         
                 cstmt.setLong(1, (Integer)bean.get("an_npretailerid"));
                 cstmt.setLong(2, (Integer)bean.get("an_npstoreid"));
                 cstmt.setLong(3, (Integer)bean.get("an_npposid"));
                 cstmt.setLong(4, (Integer)bean.get("an_nppromoterid"));
                 cstmt.setString(5, (String)bean.get("av_fromdate"));
                 cstmt.setString(6, (String)bean.get("av_todate"));
                 cstmt.setLong(7, (Integer)bean.get("an_npordertype"));
                 cstmt.setLong(8, (Integer)bean.get("an_nporderstatus"));
                 cstmt.setLong(9, (Integer)bean.get("an_npsolutioncode"));            
                 cstmt.setLong(10, (Integer) bean.get("an_plantarif"));
                 cstmt.setLong(11, (Integer)bean.get("an_productid"));
                 cstmt.setLong(12, (Integer)bean.get("an_locationid"));
                 cstmt.setLong(13, (Integer)bean.get("an_salezone"));
                 cstmt.setLong(14, (Integer)bean.get("an_npsupervisornextelid"));
                 cstmt.setString(15, (String)bean.get("av_npdepartmentid"));
                 cstmt.setString(16, (String)bean.get("av_npprovinceid"));
                 bean = null;

                 cstmt.registerOutParameter(17, OracleTypes.CURSOR);
                 cstmt.registerOutParameter(18, Types.VARCHAR);

                 cstmt.execute();

                 strMessage = cstmt.getString(18);
                 boolean resultado=false;
                 resultado=this.handleErrorResult(strMessage);

                 if (resultado) {
                     lstLiquidation = new ArrayList<NpLiquidation>();                
                     rs = (ResultSet)cstmt.getObject(17);
                     while (rs.next()) {
                         NpLiquidation liquidBean = new NpLiquidation();
                         
                         liquidBean.setNporderdate(rs.getString("FECHA"));
                         liquidBean.setNporderhour(rs.getString("HORA"));
                         liquidBean.setNptyperetailer(rs.getString("TIPO_CADENA"));
                         liquidBean.setNpdepartment(rs.getString("NPDEPARTAMENT"));
                         liquidBean.setNpprovince(rs.getString("NPPROVINCE"));
                         liquidBean.setNplocalidad(rs.getString("LOCALIDAD"));
                         liquidBean.setNpretailer(rs.getString("CADENA"));
                         liquidBean.setNpstore(rs.getString("SUCURSAL"));
                         liquidBean.setNpsocionegocio(rs.getString("NPSOCIONEGOCIO"));
                         liquidBean.setNpsupervisornextel(rs.getString("NPSUPERVISORNEXTELNAME"));
                         liquidBean.setNppromotor(rs.getString("PROMOTOR"));
                         liquidBean.setNpdnipromotor(rs.getString("NPDOCUMENTNUMBERPRO"));
                         liquidBean.setNppromotername(rs.getString("PROMOTERNAME"));
                         liquidBean.setNpordernumber(rs.getString("NUMERO_ORDEN"));
                         liquidBean.setNpordertypename(rs.getString("TIPO_ORDEN"));
                         liquidBean.setNpdivision(rs.getString("DIVISION"));
                         liquidBean.setNpsolution(rs.getString("SOLUCION"));
                         liquidBean.setNpcodclient(rs.getLong("COD_CLIENTE"));
                         liquidBean.setNpcliente(rs.getString("CLIENTE"));
                         liquidBean.setNpdocumenttype(rs.getString("TIPDOCNUMBER"));
                         liquidBean.setNpdocumentnumber(rs.getString("DOCNUMBER"));
                         liquidBean.setNpcustportvalue(rs.getString("CUST_PORT_VALUE"));
                         liquidBean.setNpcustomerbusinness(rs.getString("CUSTOMER_BUSINESS"));
                         liquidBean.setNpproducto(rs.getString("NOMBRE_PRODUCTO"));
                         liquidBean.setNpsku(rs.getString("SKU"));
                         liquidBean.setNpmodalidad(rs.getString("NPMODALIDAD"));
                         liquidBean.setNpverificacion(rs.getString("VERIFICACION"));
                         liquidBean.setNpconexion(rs.getString("CONEXION"));
                         liquidBean.setNpmodelo(rs.getString("MODELO"));
                         liquidBean.setNpplantarif(rs.getString("PLAN_TARIFARIO"));
                         liquidBean.setNppriceequip(rs.getFloat("PRECIO_EQUIPO")); 
                         liquidBean.setNppricekit(rs.getFloat("PRECIO_KIT")); //
						 ///VPEREZ 20/09/2017 PRY-0925 Inicio
                         liquidBean.setNpMontoFinanciar(rs.getFloat("MONTO_FINANCIAR")); //VEP
                         liquidBean.setNpNroCuotas(rs.getString("NRO_CUOTA")); //VEP
						 ///VPEREZ 20/09/2017 PRY-0925 Fin
                         liquidBean.setNpflagdepoguarantee(rs.getString("FLAG_DEPOSITO_GARANTIA"));                        
                         liquidBean.setNpskudepoguarantee(rs.getString("SKU_DEPO_GARANTIA"));
                         liquidBean.setNpcostguarantee(rs.getFloat("COST_GUARANTEE"));
                         liquidBean.setNpvoucherguarantee(rs.getString("VOUCHER_GUARANTEE"));
                         liquidBean.setNptypepay(rs.getString("TIPO_PAGO"));
                         liquidBean.setNpvoucher(rs.getString("VOUCHER"));
                         liquidBean.setNpnroguia(rs.getString("NRO_GUIA"));
                         liquidBean.setNpimei(rs.getString("IMEI"));
                         liquidBean.setNpsim(rs.getString("SIM"));
                         liquidBean.setNpnrocontrato(rs.getString("NRO_CONTRATO"));
                         liquidBean.setNpnumberphone(rs.getString("NUMERO_TELEFONO"));
                         liquidBean.setNpstatusorder(rs.getString("ESTADO_ORDEN"));
                         liquidBean.setNpstatuseval(rs.getString("ESTADO_EVALUACION"));
                         liquidBean.setNpriesgofinanc(rs.getString("RIESGO_FINANC"));
                         liquidBean.setNpcapaendeu(rs.getString("CAPAC_ENDEU"));
                         liquidBean.setNpcantequip(rs.getString("CANTI_EQUIPOS"));
                         liquidBean.setNpsalezone(rs.getString("ZONA_VENTA"));
                         liquidBean.setNptypewarranty(rs.getString("NPTYPEWARRANTY"));
                         //FBERNALES - NUEVOS 
                         liquidBean.setNpNumPorta(rs.getString("NUMPORTA"));
                         liquidBean.setNpOrigenPorta(rs.getString("ORIGENPORTA"));
                         liquidBean.setNpEstadoPorta(rs.getString("ESTADOPORTA"));
                         liquidBean.setNpCedentePorta(rs.getString("CEDENTEPORTA"));
                         liquidBean.setNpMotivoRecPorta(rs.getString("MOTIVORECPORTA"));
                         liquidBean.setNpMontoAdeudadoPorta(rs.getString("MONTOADEUDADOPORTA"));
                         liquidBean.setNpMonedaPorta(rs.getString("MONEDAPORTA"));
                         liquidBean.setNpFecVecUlFactPorta(rs.getString("FECVECULTFACTPORTA"));
                         liquidBean.setNpFecEjecucionPorta(rs.getString("FECEJECUCIONPORTA"));
                         liquidBean.setNpTypeOperation(rs.getString("NPOPERATIONTYPE"));
                         lstLiquidation.add(liquidBean);
                     }               
                     
                 } else {                  
                   logger.error("[NPProductDAO][Metodo: getLiquidationReport][SP: "+storedProcedure+"][MensajeError: "+strMessage+"]"); 
                 } 
                 
             } catch(SQLException sq){
                 throw  new  UnhandledException( "getLiquidationReport [ "+ Constant.PARAMETER_ERROREXCEPTION +" ] "+ sq.getMessage(),sq);
                
             }catch(Exception e){
                  throw  new  UnhandledException( "getLiquidationReport [ "+ Constant.PARAMETER_ERROREXCEPTION +" ] "+ e.getMessage(),e);
             }finally{
                 ConnectionDB.closeSTMT(conn, cstmt, rs);
             }
             return lstLiquidation;
         }
    
    /*---------------------------------------------------------------------------------------------------------------------
    -- Purpose: Retorna la Lista de Productos Disponibles por Cobertura y POS
    -- MODIFICATION HISTORY
    -- Person     Date         Comments
    -- ---------  ----------   ------------------------------------------------------------------------------------------
    -- YRUIZ      08/10/2013   SAR N_O000006450, Gerenación del Nuevo Reporte "Equipos > Cobertura por Punto de Venta"
    */
    
    public List<BeanCoveragePointOfSale> getCoverageProductByPOS(BeanCoveragePointOfSale bean)  {                
    
        Connection conn=null;
        String storedProcedure;
        CallableStatement stmt=null;
        String av_message;   
        
        OracleCallableStatement cstmt = null;
        
        String strMessage;
        ResultSet rs = null;
        ArrayList<BeanCoveragePointOfSale> lstDetail = null;
        
        storedProcedure = "begin RETAIL.NP_REPORT_PKG.sp_get_coverage_product_by_pos(?,?,?, ?, ?); end;";
        try {
            conn = ConnectionDB.getConnection();
            cstmt = (OracleCallableStatement)conn.prepareCall(storedProcedure);
            cstmt.setLong(1, bean.getNpgiroid());
            cstmt.setLong(2, bean.getNpretailerid());
            cstmt.setLong(3, bean.getNpstoreid());          
cstmt.setLong(4, bean.getNpdias());
            cstmt.setLong(5, bean.getNpmodality());       
            cstmt.registerOutParameter(6, OracleTypes.CURSOR);
            cstmt.registerOutParameter(7, Types.VARCHAR);
            cstmt.execute();
            strMessage = cstmt.getString(7);
            
            if (strMessage != null) {
                throw new Exception(strMessage);
            } else {
                lstDetail = new ArrayList<BeanCoveragePointOfSale>();                
                rs = (ResultSet)cstmt.getObject(6);
                while (rs.next()) {
                    BeanCoveragePointOfSale detail = new BeanCoveragePointOfSale();
                    
                    detail.setNpGiro((String)rs.getString("tc_giro")+"");
                    detail.setNpCadena((String)rs.getString("ret_cadena")+"");
                    detail.setNpSucursal((String)rs.getString("ret_sucursal")+"");
                    detail.setNpPOSName((String)rs.getString("pos_name")+"");
                    detail.setNpProductLine((String)rs.getString("product_line")+"");
                    detail.setNpTipoAlmacen((String)rs.getString("almacen"));
                    detail.setNpProductName((String)rs.getString("product_name")+"");
                    detail.setNpProdVendido7d((Long)rs.getLong("product_vendido_7d"));
                    detail.setNpProdVendido15d((Long)rs.getLong("product_vendido_15d"));                        
                    detail.setNpProdVendido1M((Long)rs.getLong("product_vendido_1m"));
                    detail.setNpRecogidoVendido((Long)rs.getLong("recogido_vendido"));
                    detail.setNpDispRecojoVendido((Long)rs.getLong("disp_recojo_vendido"));
                    detail.setNpDisponible((Long)rs.getLong("disponible"));
                    detail.setNpTransitoMenor7d((Long)rs.getLong("transito_menor_7d"));
                    detail.setNpTransitoMayor7d((Long)rs.getLong("transito_mayor_7d"));
                    detail.setNpCoberturaDia((String)rs.getString("cobertura_dia"));
                    detail.setNpCoberturaSemana((String)rs.getString("cobertua_semana"));
                    lstDetail.add(detail);
                }                
            }//Fin Else
            
        } catch(SQLException sq){
            throw  new  UnhandledException( "getCoverageProductByPOS [ "+ Constant.PARAMETER_ERROREXCEPTION +" ] "+ sq.getMessage(),sq);
        }catch(Exception e){
            throw  new  UnhandledException( "getCoverageProductByPOS [ "+ Constant.PARAMETER_ERROREXCEPTION +" ] "+ e.getMessage(),e);
        }finally{
            ConnectionDB.closeSTMT(conn, cstmt, rs);
        }
        return lstDetail;
    }
    
    /*---------------------------------------------------------------------------------------------------------------------
    -- Purpose: Retorna la Lista de Kist asignados, filtrados por Modelo, Plan y Precio.
    -- MODIFICATION HISTORY
    -- Person     Date         Comments
    -- ---------  ----------   ------------------------------------------------------------------------------------------
    -- YRUIZ      10/03/2014   N_O000014808  - Desasociar SKU - Retail "Asignación Masiva de SKU > Desasociar SKU masivo"
    */
     public List<SkuProductxIdRetail> getKitsSkuByModelPlan(NpProduct beanModelo, NpProduct beanProduct, NpRetailer beanRetailer) {
             
             Connection conn=null;
             String storedProcedure;
             CallableStatement stmt=null;
             String av_message;   
             
             ResultSet rs = null;
             av_message = null;
             storedProcedure = "begin NPSKU_PRODUCT_PKG.SP_GET_REGISTEREDKITXSKU_LST(?,?,?,?,?,?,?,?); end;";
             List<SkuProductxIdRetail> lstProductxSKU = new ArrayList<SkuProductxIdRetail>();
             try{
                     conn = ConnectionDB.getConnection();                   
                     stmt = conn.prepareCall(storedProcedure);
                     stmt .setLong(1, beanModelo.getNpproductid());
                     stmt .setLong(2, beanProduct.getNpproductid());
                     stmt .setLong(3, beanProduct.getNpplanid()); 
                     stmt.setFloat(4, beanProduct.getNpcost()); 
                     stmt.setLong(5, beanRetailer.getNpretailerid());
                     stmt.setString(6, beanProduct.getSkuProduct());
                     stmt .registerOutParameter(7, OracleTypes.CURSOR);
                     stmt .registerOutParameter(8, Types.VARCHAR);

                     stmt.execute();
                     av_message = (String)stmt.getObject(8);
                     boolean resultado=false;
                     resultado=this.handleErrorResult(av_message);
                      
                     if(resultado){
                            rs = (ResultSet)stmt.getObject(7);
                            lstProductxSKU = new ArrayList<SkuProductxIdRetail>();  
                                 
                             while(rs.next()){
                                     SkuProductxIdRetail productxSKU = new SkuProductxIdRetail();                                     
                                     productxSKU.setSkuproductId((Long)rs.getLong("npskuproductid"));                                     
                                     productxSKU.setProductId((Long)rs.getLong("npproductid"));
                                     productxSKU.setNpproductname((String)rs.getString("npproductname")+"");
                                     productxSKU.setNpretailername((String)rs.getString("npretailername")+"");
                                     productxSKU.setSku((String)rs.getString("npsku")+"");
                                     productxSKU.setNpcost((Float)rs.getFloat("npcostokit"));
                                     productxSKU.setNpcurrency((Integer)rs.getInt("npcurrencykit"));
                                     productxSKU.setNpplan_id((Long)rs.getLong("npplanid"));
                                     productxSKU.setNpplan_name((String)rs.getString("npplanname")+""); 
                                     productxSKU.setNpcost_plan((Float)rs.getFloat("npcostplan"));
                                     productxSKU.setNpspecialprice((Integer)rs.getInt("npspecialprice"));
                                     productxSKU.setNpgeographiczone((String)rs.getString("npzonaGeografica"));
                                     productxSKU.setNpmodelid((Long)rs.getLong("npmodelid"));
                                     productxSKU.setNpmodelname((String)rs.getString("npmodelname"));
                                     productxSKU.setNpservicename((String)rs.getString("npservicename"));
                                     lstProductxSKU.add(productxSKU);
                             }
                     }else{
                        logger.error("[NPProductDAO][Metodo: getKitsSkuByModelPlan][SP: "+storedProcedure+"][MensajeError: "+av_message+"]");
                      }
                      
             }catch (Exception e){
                 throw  new  UnhandledException( "getKitsSkuByModelPlan [ "+ Constant.PARAMETER_ERROREXCEPTION +" ] "+ e.getMessage(),e);
             }finally{
                 ConnectionDB.close(conn, rs, stmt);
             }
             return(lstProductxSKU);
     }
     
    public List<NpPreviewConsult> getPreviewConsultReport(Map bean) {
        OracleCallableStatement cstmt = null;        
        Connection conn = null;
        String strMessage;
        String storedProcedure = null;
        ResultSet rs = null;
        ArrayList<NpPreviewConsult> lstPreviewConsult = null;
        
        storedProcedure = "begin RETAIL.NPPORTABILITY_PKG.SP_GET_PREVIEW_CONSULT(?,?,?,?,?,?,?,?,?,?,?); end;";
        try {
            conn = ConnectionDB.getConnection();
            cstmt = (OracleCallableStatement)conn.prepareCall(storedProcedure);             
            cstmt.setLong(1, (Long)bean.get("an_npretailerid"));
            cstmt.setLong(2, (Long)bean.get("an_npstoreid"));
            cstmt.setLong(3, (Long)bean.get("an_npposid"));
            cstmt.setLong(4, (Long)bean.get("an_nppromoterid"));
            cstmt.setString(5,(String)bean.get("an_npdocumenttype"));
            if(StringUtils.IsNullorEmpty((String)bean.get("an_npdocumentnumber")))
                cstmt.setNull(6,OracleTypes.NULL);
            else
                cstmt.setString(6,(String)bean.get("an_npdocumentnumber"));
                
            cstmt.setString(7, (String)bean.get("av_fromdate"));
            cstmt.setString(8, (String)bean.get("av_todate"));    
            
            if(StringUtils.IsNullorEmpty((String)bean.get("av_nro_telefono")))
                cstmt.setNull(9,OracleTypes.NULL);
            else
                cstmt.setString(9, (String)bean.get("av_nro_telefono"));  

            cstmt.registerOutParameter(10, Types.VARCHAR);
            cstmt.registerOutParameter(11, OracleTypes.CURSOR);
            
            cstmt.execute();
            
            strMessage = cstmt.getString(10);
            boolean resultado=false;
            resultado=this.handleErrorResult(strMessage);
            
            if (resultado) {
                lstPreviewConsult = new ArrayList<NpPreviewConsult>();                
                rs = (ResultSet)cstmt.getObject(11);
                while (rs.next()) {                     
                    NpPreviewConsult liqui = new NpPreviewConsult();                 
                   
                    liqui.setNporderhour(StringUtils.notNull(rs.getString("HORA"),Constant.TYPE_STRING));
                    liqui.setNpretailer(rs.getString("CADENA")== null ? " " : rs.getString("CADENA"));
                    liqui.setNpstore((String)rs.getString("SUCURSAL")== null ? " " : rs.getString("SUCURSAL"));
                    liqui.setNppos((String)rs.getString("PUNTO_DE_VENTA")== null ? " " : rs.getString("PUNTO_DE_VENTA"));
                    liqui.setNppromotor((String)rs.getString("PROMOTOR")== null ? " " : rs.getString("PROMOTOR"));
                    liqui.setNpdateconsult((String)rs.getString("FECHA_DE_LA_CONSULTA")== null ? " " : rs.getString("FECHA_DE_LA_CONSULTA"));
                    liqui.setNpnombres((String)rs.getString("NOMBRES_APELLIDOS")== null ? " " : rs.getString("NOMBRES_APELLIDOS"));
                    liqui.setNpemail((String)rs.getString("CORREO")== null ? " " : rs.getString("CORREO"));
                    liqui.setNptelephonenumber((String)rs.getString("TELEFONO")== null ? " " : rs.getString("TELEFONO"));
                    liqui.setNporigen((String)rs.getString("MODALIDAD")== null ? " " : rs.getString("MODALIDAD"));  
                    liqui.setNpdocumenttype((String)rs.getString("TIPO_DE_DOCUMENTO")== null ? " " : rs.getString("TIPO_DE_DOCUMENTO"));
                    liqui.setNpdocumentnumber((String)rs.getString("N_DOCUMENTO")== null ? " " : rs.getString("N_DOCUMENTO"));
                    liqui.setNpconsultresult((String)rs.getString("RESULTADO_DE_LA_CONSULTA")== null ? " " : rs.getString("RESULTADO_DE_LA_CONSULTA"));
                    liqui.setNprejectionreason((String)rs.getString("MOTIVO_RECHAZO")== null ? " " : rs.getString("MOTIVO_RECHAZO"));
                    liqui.setNpactivationdate((String)rs.getString("FECHA_ACTIVACION")== null ? " " : rs.getString("FECHA_ACTIVACION"));
                    liqui.setNpEstadoConsultaPrevia((String)rs.getString("Cons_Prev_Port")== null ? " " : rs.getString("Cons_Prev_Port"));
                    liqui.setNpCedente((String)rs.getString("CEDENTE")== null ? " " : rs.getString("CEDENTE"));
                    lstPreviewConsult.add(liqui);
                }               
                
            }else{
              logger.error("[NPProductDAO][Metodo: getPreviewConsultReport][SP: "+storedProcedure+"][MensajeError: "+strMessage+"]");
            }
        } catch(SQLException sq){
            throw  new  UnhandledException( "getPreviewConsultReport [ "+ Constant.PARAMETER_ERROREXCEPTION +" ] "+ sq.getMessage(),sq);
        }catch(Exception e){
            throw  new  UnhandledException( "getPreviewConsultReport [ "+ Constant.PARAMETER_ERROREXCEPTION +" ] "+ e.getMessage(),e);
        }finally{
            ConnectionDB.closeSTMT(conn, cstmt, rs);
        }
        return lstPreviewConsult;
    }
    
    public Long insDevolutionPromo(NpDevolutionProm bean)  {
        String storedProcedure = 
            "begin RETAIL.NPDEVOLUTION_PROMO_PKG.SP_INS_DEVOLUTION_PROMO(?,?,?,?,?,?,?,?,?,?,?,?,?,?); end;";
        Connection conn = null;
        OracleCallableStatement stmt = null;
        ResultSet rs = null;        
        Long bReturn=0L;
        try {
            conn = ConnectionDB.getConnection();
            stmt = (OracleCallableStatement)conn.prepareCall(storedProcedure);            
            stmt.setLong(1,bean.getNpwaybillid());
            stmt.setString(2,bean.getNpwaybillnumber());
            stmt.setLong(3,bean.getNpdevolutionsubtype());
            stmt.setLong(4,bean.getNpposid());
            stmt.setLong(5,bean.getNptypedocument());
            stmt.setString(6,bean.getNpdocumentnumber());
            stmt.setString(7,bean.getNpcustomerfirstname());
            stmt.setString(8,bean.getNpcustomerlastname());
            stmt.setString(9,bean.getNpcreatedby());
            stmt.setLong(10,bean.getNpcountitems());
            stmt.registerOutParameter(11, Types.VARCHAR);
            stmt.registerOutParameter(12, Types.NUMERIC);
            stmt.registerOutParameter(13, Types.NUMERIC);
            stmt.registerOutParameter(14, Types.VARCHAR);
            
            stmt.execute();
            String av_message = (String)stmt.getObject(11);
            boolean resultado=false;
            resultado=this.handleErrorResult(av_message);
            
            if (resultado) {
                bReturn = new Long(String.valueOf(stmt.getObject(12)));
                Long npdevolutioncodeid = new Long(String.valueOf(stmt.getObject(13)));
                if (npdevolutioncodeid>0){
                    String npdevolutioncodenumber = (String)stmt.getObject(14);
                    bean.setNpdevolutioncodeid(npdevolutioncodeid);
                    bean.setNpdevolutioncodenumber(npdevolutioncodenumber);
                }else{
                    bReturn=0L;
                }
                bean.setNpdevolutionid(bReturn);
            } else {
                bReturn=0L;
              logger.error("[NPProductDAO][Metodo: insDevolutionPromo][SP: "+storedProcedure+"][MensajeError: "+av_message+"]");
            }
        }catch (Exception e){
            throw  new  UnhandledException( "insDevolutionPromo [ "+ Constant.PARAMETER_ERROREXCEPTION +" ] "+ e.getMessage(),e);
        } finally {
            ConnectionDB.closeSTMT(conn, stmt, rs);
        }
        return bReturn;
    }
    
    public Boolean insDevolutionDetailPromo(NpDevolutionDetailProm bean)  {
        String storedProcedure = 
            "begin RETAIL.NPDEVOLUTION_PROMO_PKG.SP_INS_DEVOLUTION_PROMO_DETAIL(?,?,?,?,?,?,?,?,?,?,?,?); end;";
        Connection conn = null;
        OracleCallableStatement stmt = null;
        ResultSet rs = null;        
        Boolean bReturn=true;
        try {
            conn = ConnectionDB.getConnection();
            stmt = (OracleCallableStatement)conn.prepareCall(storedProcedure);            
            stmt.setLong(1,bean.getNpdevolutionid());
            stmt.setString(2,bean.getNpimei());
            stmt.setString(3,bean.getNpsim());
            stmt.setLong(4,bean.getNpproductid());
            stmt.setLong(5,bean.getNpmotiveid());
            stmt.setLong(6,bean.getNpsubwarehouseid());
            stmt.setLong(7,bean.getNpdevolutioncodeid());
            stmt.setString(8,bean.getNpdevolutioncodenumber());
            stmt.setString(9,bean.getNpstatusbegin());
            stmt.setString(10,bean.getNpstatusend());
            stmt.setString(11,bean.getNpcreatedby());
            stmt.registerOutParameter(12, Types.VARCHAR);            
            stmt.execute();
            String av_message = (String)stmt.getObject(12);
            boolean resultado=false;
            resultado=this.handleErrorResult(av_message);
            
            if (resultado) {
                bReturn = true;
            } else {
                bReturn=false;
                logger.error("[NPProductDAO][Metodo: insDevolutionDetailPromo][SP: "+storedProcedure+"][MensajeError: "+av_message+"]");
            }
        }catch (Exception e){
                throw  new  UnhandledException( "insDevolutionDetailPromo [ "+ Constant.PARAMETER_ERROREXCEPTION +" ] "+ e.getMessage(),e); 
        } finally {
            ConnectionDB.closeSTMT(conn, stmt, rs);
        }
        return bReturn;
    }

   public List<NpProduct> getModelosXLinea(Integer productLineId)  {
   
       String storedProcedure = "BEGIN NPPRODUCT_PKG.SP_GET_PLANS_X_LINEA(?,?,?); END;";
       Connection conn = null;
       CallableStatement stmt = null;
       ResultSet rs = null;
       ArrayList<NpProduct> lstPlans = null;
       try {
           conn = ConnectionDB.getConnection();
           stmt = conn.prepareCall(storedProcedure);
           stmt.setLong(1, productLineId);
           stmt.registerOutParameter(2, OracleTypes.CURSOR);
           stmt.registerOutParameter(3, Types.VARCHAR);
           stmt.execute();
           String strMessage = stmt.getString(3);
           boolean resultado=false;
           resultado=this.handleErrorResult(strMessage);
           
           if (resultado) {
               lstPlans = new ArrayList<NpProduct>();
               rs = (ResultSet)stmt.getObject(2);
               while (rs.next()) {
                  NpProduct product = new NpProduct();
                  product.setNpproductid(rs.getLong("NPPRODUCTID"));
                  product.setNpproductname(StringUtils.notNull(rs.getString("NPPRODUCTNAME"), Constant.TYPE_STRING));
                  lstPlans.add(product);
               }
           }else{
             logger.error("[NPProductDAO][Metodo: getModelosXLinea][SP: "+storedProcedure+"][MensajeError: "+strMessage+"]" +
                          "[Parametros][productLine:"+productLineId+"]");
           }
           
       } catch(Exception e){
           throw  new  UnhandledException( "getModelosXLinea [ "+ Constant.PARAMETER_ERROREXCEPTION +" ] "+ e.getMessage(),e);
       } finally {
          ConnectionDB.closeSTMT(conn, stmt, rs);
       }
       return lstPlans;
   }

    public Boolean updDevolutionImeiPromo(Long Npdevolutionid, String lStatusImei)  {
        String storedProcedure = 
            "begin RETAIL.NPDEVOLUTION_PROMO_PKG.SP_UPD_DEVOLUTION_PROMO_IMEI(?,?,?); end;";
        Connection conn = null;
        OracleCallableStatement stmt = null;
        ResultSet rs = null;        
        Boolean bReturn=true;
        try {
            conn = ConnectionDB.getConnection();
            stmt = (OracleCallableStatement)conn.prepareCall(storedProcedure);            
            stmt.setLong(1,Npdevolutionid);
            stmt.setString(2,lStatusImei);            
            stmt.registerOutParameter(3, Types.VARCHAR);
            stmt.execute();
            String av_message = (String)stmt.getObject(3);
            boolean resultado;
            resultado=this.handleErrorResult(av_message);
            if (resultado) {
                bReturn = true;
            } else {
                bReturn=false;
              logger.error("[NPProductDAO][Metodo: getModelosXLinea][SP: "+storedProcedure+"][MensajeError: "+av_message+"]" +
                           "[Parametros][Npdevolutionid:"+Npdevolutionid+"][lStatusImei:"+lStatusImei+"]");
            }
        }catch (Exception e){
            throw  new  UnhandledException( "updDevolutionImeiPromo [ "+ Constant.PARAMETER_ERROREXCEPTION +" ] "+ e.getMessage(),e);   
        } finally {            
            ConnectionDB.closeSTMT(conn, stmt, rs);
        }
        return bReturn;
    }
    
    public List<NpPlan> getPlanesXLinea(Integer productLineId) {
       
           String storedProcedure = "BEGIN NPPRODUCT_PKG.SP_GET_PLANS_X_LINEAPRODUCTO(?,?,?); END;";
           Connection conn = null;
           CallableStatement stmt = null;
           ResultSet rs = null;
           ArrayList<NpPlan> lstPlans = null;
           try {
               conn = ConnectionDB.getConnection();
               stmt = conn.prepareCall(storedProcedure);
               stmt.setLong(1, productLineId);
               stmt.registerOutParameter(2, OracleTypes.CURSOR);
               stmt.registerOutParameter(3, Types.VARCHAR);
               stmt.execute();
               String strMessage = stmt.getString(3);
               boolean resultado=false;
               resultado=this.handleErrorResult(strMessage);
               if (resultado) {
                   lstPlans = new ArrayList<NpPlan>();
                   rs = (ResultSet)stmt.getObject(2);
                   while (rs.next()) {
                      NpPlan plan = new NpPlan();
                      plan.setNpplanid(rs.getLong("NPPLANID"));
                      plan.setNpplanname(StringUtils.notNull(rs.getString("NPPLANNAME"), Constant.TYPE_STRING));
                      lstPlans.add(plan);
                   }
               }else{
                 logger.error("[NPProductDAO][Metodo: getPlanesXLinea][SP: "+storedProcedure+"][MensajeError: "+strMessage+"]" +
                              "[Parametros][productLineId:"+productLineId+"]");
               }
           } catch(Exception e){
               throw  new  UnhandledException( "getPlanesXLinea [ "+ Constant.PARAMETER_ERROREXCEPTION +" ] "+ e.getMessage(),e);
           } finally {
              ConnectionDB.closeSTMT(conn, stmt, rs);
           }
           return lstPlans;
       }
       
    public List<NpProduct> getPreciosXLinea(Integer productLineId, Integer productId) {
       
           String storedProcedure = "BEGIN NPPRODUCT_PKG.SP_GET_PRECIOS_X_LINEAPRODUCTO(?,?,?,?); END;";
           Connection conn = null;
           CallableStatement stmt = null;
           ResultSet rs = null;
           ArrayList<NpProduct> products = null;
           try {
               conn = ConnectionDB.getConnection();
               stmt = conn.prepareCall(storedProcedure);
               stmt.setLong(1, productLineId);
               stmt.setLong(2, productId);
               stmt.registerOutParameter(3, OracleTypes.CURSOR);
               stmt.registerOutParameter(4, Types.VARCHAR);
               stmt.execute();
               String strMessage = stmt.getString(4);
               boolean resultado=false;
               resultado=this.handleErrorResult(strMessage);
               
               if (resultado) {
                   products = new ArrayList<NpProduct>();
                   rs = (ResultSet)stmt.getObject(3);
                   while (rs.next()) {
                      NpProduct product = new NpProduct();
                      product.setNpcost(rs.getFloat("NPPRICE"));
                      products.add(product);
                   }
               }else{
                 logger.error("[NPProductDAO][Metodo: getPlanesXLinea][SP: "+storedProcedure+"][MensajeError: "+strMessage+"]" +
                              "[Parametros][productLineId:"+productLineId+"][productId:"+productId+"]");
               }
               
               
           }catch(Exception e){
               throw  new  UnhandledException( "getPreciosXLinea [ "+ Constant.PARAMETER_ERROREXCEPTION +" ] "+ e.getMessage(),e);
           } finally {
              ConnectionDB.closeSTMT(conn, stmt, rs);
           }
           return products;
       }
       
 public List<NpProduct> getKitsXModeloPlanPrecio(Integer productLineId ,Integer retailerId, Integer productId, Integer planId, Float precio)  {
       
           String storedProcedure = "BEGIN NPPRODUCT_PKG.SP_GET_KITS(?,?,?,?,?,?,?); END;";
           Connection conn = null;
           CallableStatement stmt = null;
           ResultSet rs = null;
           ArrayList<NpProduct> products = null;
           try {
               conn = ConnectionDB.getConnection();
               stmt = conn.prepareCall(storedProcedure);
               //se registran los parametros de entrada
               stmt.setLong(1, productLineId);
               stmt.setLong(2,retailerId);
               stmt.setLong(3,productId);
               stmt.setLong(4,planId);
               if(precio != null){
                   stmt.setFloat(5,Float.valueOf(precio));
               }else {
                   stmt.setNull(5,OracleTypes.FLOAT);
               }
               //se registran los parametros de salida
               stmt.registerOutParameter(6, OracleTypes.CURSOR);
               stmt.registerOutParameter(7, Types.VARCHAR);
               stmt.execute();
               String strMessage = stmt.getString(7);
               boolean resultado=false;
               resultado=this.handleErrorResult(strMessage);
               
               if (resultado) {
                   products = new ArrayList<NpProduct>();
                   rs = (ResultSet)stmt.getObject(6);
                   while (rs.next()) {
                    NpProduct product = new NpProduct();
                    product.setNpproductid(rs.getLong("NPPRODUCTID"));
                    product.setNpproductname(StringUtils.notNull(rs.getString("NPPRODUCTNAME"), Constant.TYPE_STRING));
                    products.add(product);
                   }
               }else{
                 logger.error("[NPProductDAO][Metodo: getKitsXModeloPlanPrecio][SP: "+storedProcedure+"][MensajeError: "+strMessage+"]");
               }
               
           }catch(Exception e){
                  throw  new  UnhandledException( "getKitsXModeloPlanPrecio [ "+ Constant.PARAMETER_ERROREXCEPTION +" ] "+ e.getMessage(),e);
           } finally {
              ConnectionDB.closeSTMT(conn, stmt, rs);
           }
           return products;
       }
       
    public List<NpProduct> getPreciosXKit(Integer productLineId, Integer productId, Integer idKit)   {
           String storedProcedure = "BEGIN NPPRODUCT_PKG.SP_GET_PRECIOS_X_KIT(?,?,?,?,?); END;";
           Connection conn = null;
           CallableStatement stmt = null;
           ResultSet rs = null;
           ArrayList<NpProduct> products = null;
           try {
               conn = ConnectionDB.getConnection();
               stmt = conn.prepareCall(storedProcedure);
               stmt.setLong(1, productLineId);
               stmt.setLong(2, productId);
               stmt.setLong(3, idKit);
               stmt.registerOutParameter(4, OracleTypes.CURSOR);
               stmt.registerOutParameter(5, Types.VARCHAR);
               stmt.execute();
               String strMessage = stmt.getString(5);
               boolean resultado=false;
               resultado=this.handleErrorResult(strMessage);
               if (resultado) {
                   products = new ArrayList<NpProduct>();
                   rs = (ResultSet)stmt.getObject(4);
                   while (rs.next()) {
                      NpProduct product = new NpProduct();
                      product.setNpcost(rs.getFloat("NPPRICE"));
                      products.add(product);
                   }
               }else{
                 logger.error("[NPProductDAO][Metodo: getPreciosXKit][SP: "+storedProcedure+"][MensajeError: "+strMessage+"]" +
                              "[Parametros][productLineId:"+productLineId+"][productId:"+productId+"][idKit:"+idKit+"]"); 
               }
               
           } catch (Exception e){
                   throw  new  UnhandledException( "getPreciosXKit [ "+ Constant.PARAMETER_ERROREXCEPTION +" ] "+ e.getMessage(),e);
           } finally {
              ConnectionDB.closeSTMT(conn, stmt, rs);
           }
           return products;
       }
       
       
    public List<NpProduct> getsku(Integer productId, Integer retailerid)   {
           String storedProcedure = "BEGIN NPPRODUCT_PKG.SP_GET_SKU_X_IDPRODUC(?,?,?,?); END;";
           Connection conn = null;
           CallableStatement stmt = null;
           ResultSet rs = null;
           ArrayList<NpProduct> products = null;
           try {
               conn = ConnectionDB.getConnection();
               stmt = conn.prepareCall(storedProcedure);
               stmt.setLong(1, productId);
               stmt.setLong(2, retailerid);
               stmt.registerOutParameter(3, OracleTypes.CURSOR);
               stmt.registerOutParameter(4, Types.VARCHAR);
               stmt.execute();
               String strMessage = stmt.getString(4);
               boolean resultado=false;
               resultado=this.handleErrorResult(strMessage);
               
               if (resultado) {
                   products = new ArrayList<NpProduct>();
                   rs = (ResultSet)stmt.getObject(3);
                   while (rs.next()) {
                      NpProduct product = new NpProduct();
                      product.setSkuProduct(rs.getString("NPSKU"));
                      products.add(product);
                   }
               }else{
                     logger.error("[NPProductDAO][Metodo: getPreciosXKit][SP: "+storedProcedure+"][MensajeError: "+strMessage+"]" +
                                  "[Parametros][productId:"+productId+"][retailerid:"+retailerid+"]");
               }
               
           }catch(Exception e){
                throw  new  UnhandledException( "getsku [ "+ Constant.PARAMETER_ERROREXCEPTION +" ] "+ e.getMessage(),e);
           } finally {
              ConnectionDB.closeSTMT(conn, stmt, rs);
           }
           return products;
       }
       
       
       
}

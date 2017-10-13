package com.nextel.action;

import com.google.gson.Gson;

import com.nextel.aditional.BeanConsultAttendanceSearch;
import com.nextel.aditional.BeanConsultSaleSearch;
import com.nextel.aditional.BeanCoveragePointOfSale;
import com.nextel.aditional.BeanPromoterPOS;
import com.nextel.aditional.BeanSaleDetail;
import com.nextel.aditional.OrderDetailConsult;
import com.nextel.aditional.SkuProductxIdRetail;
import com.nextel.bean.NpAvailableProduct;
import com.nextel.bean.NpConsolidatedEval;
import com.nextel.bean.NpConsolidatedEvaluation;
import com.nextel.bean.NpDocument;
import com.nextel.bean.NpLiquidation;
import com.nextel.bean.NpOrder;
import com.nextel.bean.NpParameter;
import com.nextel.bean.NpPortabilidad;
import com.nextel.bean.NpPortabilityCabecera;
import com.nextel.bean.NpPortabilityDetalle;
import com.nextel.bean.NpPos;
import com.nextel.bean.NpPreviewConsult;
import com.nextel.bean.NpProduct;
import com.nextel.bean.NpPromoter;
import com.nextel.bean.NpRetailer;
import com.nextel.bean.NpSolution;
import com.nextel.bean.NpStore;
import com.nextel.bean.NpSubWarehouseDetail;
import com.nextel.bean.NpUbigeo;
import com.nextel.bean.PromoterAttendanceBean;
import com.nextel.bean.ResponseBean;
import com.nextel.bean.SaleBean;

import com.nextel.bean.SaleListResp;
import com.nextel.core.ApplicationContext;
import com.nextel.core.RetailProperties;
import com.nextel.form.ConsultForm;
import com.nextel.iservice.INpEvaluationHistoryService;
import com.nextel.iservice.INpOrderItemService;
import com.nextel.iservice.INpOrderService;
import com.nextel.iservice.INpParameterService;
import com.nextel.iservice.INpPlanService;
import com.nextel.iservice.INpPortabilidadServices;
import com.nextel.iservice.INpPosService;
import com.nextel.iservice.INpProductService;
import com.nextel.iservice.INpPromoterService;
import com.nextel.iservice.INpRetailerService;
import com.nextel.iservice.INpSkuProductService;
import com.nextel.iservice.INpStoreService;
import com.nextel.iservice.INpUbigeoService;
import com.nextel.service.NpPortabilidadServices;
import com.nextel.utilities.ConnectionDB;
import com.nextel.utilities.Constant;
import com.nextel.utilities.DateUtils;
import com.nextel.utilities.NotNullStringBuilderDecorator;
import com.nextel.utilities.OrderImagesHandler;
import com.nextel.utilities.SftpUtil;
import com.nextel.utilities.SoapRequestHeaderModifier;
import com.nextel.utilities.StringUtils;

import com.nextel.utilities.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import java.sql.Connection;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.xml.bind.JAXBElement;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.upload.FormFile;

import org.springframework.ws.client.core.WebServiceTemplate;

import pe.com.entel.esb.message.gestionventa.consultarorden.v1.ConsultarOrdenRequestType;
import pe.com.entel.esb.message.gestionventa.consultarorden.v1.ConsultarOrdenResponse;
import pe.com.entel.esb.message.gestionventa.consultarorden.v1.ItemOrdenEquipoMsgType;
import pe.com.entel.esb.message.gestionventa.consultarorden.v1.ItemOrdenMsgType;
import pe.com.entel.esb.message.gestionventa.consultarorden.v1.ListaOrdenMsg;
import pe.com.entel.ws.processmessageportability.AssociatedKey;
import pe.com.entel.ws.processmessageportability.GetProcessMsgPortabilityRequest;
import pe.com.entel.ws.processmessageportability.GetProcessMsgPortabilityResponse;
import pe.com.entel.ws.processmessageportability.SendMessageBE;

import wsp.getsales.__soap_WSAP_GetSales_GetSales_pptClient;
import wsp.getsales.types.com.nextel.xmlns.retail.getsales.GetSalesRequest;
import wsp.getsales.types.com.nextel.xmlns.retail.getsales.GetSalesResponse;
import wsp.getsales.types.com.nextel.xmlns.retail.getsales.RETAILNPORDER_PKG_TT_SALES_LIST1;
import wsp.getsales.types.com.nextel.xmlns.retail.getsales.RETAILNP_TYPES_SALE_PKG_TR_SALES2;

import pe.com.entel.ws.getprocessmsgportability.CreateMessage;
import pe.com.entel.ws.getprocessmsgportability.CreateMessagePortabilityRequest;
import pe.com.entel.ws.getprocessmsgportability.CreateMessagePortabilityResponse;


/*
   * Migracion ESB a OSB
   * Se comenta las referencias para invocar al nuevo servicio DelSalesOrder con SpringWS
   * vcedeno@soaint.com
   * Nuevas referencias - vcedeno@soaint.com
*/

/*---------------------------------------------------------------------------------------------------------------------
   Purpose: Actions del tipo Consultas
   MODIFICATION HISTORY
   Person     Date         Comments
   ---------  ----------   ------------------------------------------------------------------------------------------
   YRUIZ      08/10/2013   SAR N_O000006450, Gerenación del Nuevo Reporte "Equipos > Cobertura por Punto de Venta"
   YRUIZ      17/03/2014   SAR N_O000007053, Gerenación del Nuevo Reporte "Promotores > Promotores por Punto de Venta"
*/

public class ConsultAction extends GenericAction {

    private static Logger logger = Logger.getLogger(ConsultAction.class);
    
    SoapRequestHeaderModifier headerRequest = new SoapRequestHeaderModifier();
    
    private static pe.com.entel.esb.message.gestionventa.consultarorden.v1.ObjectFactory ofConsultarOrden = new pe.com.entel.esb.message.gestionventa.consultarorden.v1.ObjectFactory();
    private static pe.com.entel.ws.processmessageportability.ObjectFactory ofProcessMessagePortability = new pe.com.entel.ws.processmessageportability.ObjectFactory(); //vcedenos@soaint.com
    private static pe.com.entel.ws.getprocessmsgportability.ObjectFactory ofGetProcessMsgPortability = new pe.com.entel.ws.getprocessmsgportability.ObjectFactory(); //vcedeno@soaint.com
    
    public ActionForward productStockDetailHome(ActionMapping mapping, 
                                                ActionForm form, 
                                                HttpServletRequest request, 
                                                HttpServletResponse response) throws Exception {
        request.getSession().setAttribute("detallestock", "1");
        request.getSession().setAttribute("productStockDetailList", null);
        return mapping.findForward("ProductStockHome");
    }

    public ActionForward productStockDetailSearch(ActionMapping mapping, 
                                                  ActionForm form, 
                                                  HttpServletRequest request, 
                                                  HttpServletResponse response) throws Exception {

        NpAvailableProduct stockDetailParameters = new NpAvailableProduct();
        List<NpSubWarehouseDetail> productStockDetailList = null;

            logger.info("Inicio productStockSearch");
            ConsultForm consultForm = (ConsultForm)form;

            INpProductService productService = 
                (INpProductService)getInstance("NpProductService");

            //Por ahora no se usa el filtro sub warehouse id:
            stockDetailParameters.setNpsubwarehouseid(0L);

            stockDetailParameters.setNpsubwarehouseType(new Long(consultForm.getCmbSubWarehouse()));
            String rol = (String)request.getSession().getAttribute("rol");
            if (rol != null && rol.equals("admin")) {
                stockDetailParameters.setNpposid(consultForm.getCmbPOS() != 
                                                 null && 
                                                 !consultForm.getCmbPOS().equals("") ? 
                                                 new Long(consultForm.getCmbPOS()) : 
                                                 0);
                stockDetailParameters.setNpproductid(consultForm.getCmbProduct() != 
                                                     null && 
                                                     !consultForm.getCmbProduct().equals("") ? 
                                                     new Long(consultForm.getCmbProduct()) : 
                                                     0);
                stockDetailParameters.setNpretailerid(consultForm.getCmbRetailer() != 
                                                      null && 
                                                      !consultForm.getCmbRetailer().equals("") ? 
                                                      new Long(consultForm.getCmbRetailer()) : 
                                                      0);
                stockDetailParameters.setNpstoreid(consultForm.getCmbStore() != 
                                                   null && 
                                                   !consultForm.getCmbStore().equals("") ? 
                                                   new Long(consultForm.getCmbStore()) : 
                                                   0);
                stockDetailParameters.setNpstatus(consultForm.getCmbState() != 
                                                  null && 
                                                  !consultForm.getCmbState().equals("") ? 
                                                  consultForm.getCmbState() : 
                                                  "0");
            } else {
                stockDetailParameters.setNpposid(new Long(consultForm.getCmbPOS()));
                stockDetailParameters.setNpproductid(new Long(consultForm.getCmbProduct()));
                stockDetailParameters.setNpretailerid(new Long(consultForm.getIdRetail()));
                stockDetailParameters.setNpstoreid(new Long(consultForm.getIdStore()));
                stockDetailParameters.setNpstatus(new String(consultForm.getCmbState()));
            }

            productStockDetailList = 
                    productService.getProductStockDetail(stockDetailParameters);

            request.getSession().setAttribute("productStockDetailList", 
                                              productStockDetailList);

            //CRM 27-05-09 PARA QUE CARGE LOS COMBOS SUCURSAL 
            NpRetailer retail = new NpRetailer();
            if (request.getParameter("cmbRetailer") != null && 
                !request.getParameter("cmbRetailer").equals(""))
                retail.setNpretailerid(new Long(request.getParameter("cmbRetailer")));
            else
                retail.setNpretailerid(null);
            NpStore store = new NpStore();
            store.setNpRetailer(retail);
            //Traemos las sucursales que pertenecen a una Cadena     
            INpStoreService storeService = 
                (INpStoreService)getInstance("NpStoreService");
            List<NpStore> listaStore = 
                storeService.getEntityByProperty(null, store);
            request.getSession().setAttribute("listaSucursal", listaStore);

            //*************
            //productosResponse.getAT_AVAILABLEPRODUCTS_LST();
            logger.info("Fin productStockSearch");
            return mapping.findForward("SearchProductStockDetail");
    }

    //Permite  inicializar las variables                

    public ActionForward productStockHome(ActionMapping mapping, 
                                          ActionForm form, 
                                          HttpServletRequest request, 
                                          HttpServletResponse response) throws Exception {

            logger.info("Inicio productStockHome");
            ConsultForm consultForm = (ConsultForm)form;

            consultForm.setTxtRetailer("");
            consultForm.setTxtStore("");
            consultForm.setTxtPOS("");
            //CMF Inicio 04/05/2009
            consultForm.setCmbRetailer("");
            consultForm.setCmbStore("");
            consultForm.setCmbPOS("");
            consultForm.setCmbProduct("");
            consultForm.setCmbSubWarehouse("");
            request.getSession().removeAttribute("listaProductosStock");
            //CMF Fin 04/05/2009
            //CRM 19-05-09 Para mostrar los combos si es administrador
            String rol = (String)request.getSession().getAttribute("rol");
            if (rol != null && rol.equals("admin")) {
                //Se carga el combo Cadena
                consultForm.setMostrarCombos(new Long(0)); // Si es cero se muestra los combos
                INpRetailerService retailService = (INpRetailerService)getInstance("NpRetailerService");
                consultForm.setLstRetail(retailService.getEntities(null));
                request.getSession().setAttribute("ListaCadena", consultForm.getLstRetail());
                INpParameterService parameterService = (INpParameterService)getInstance("NpParameterService");
                consultForm.setLstWarehouseType(parameterService.getEntityByxNameDomain(Constant.TIPOALMACEN));
                request.getSession().setAttribute("ListaAlmacen", consultForm.getLstWarehouseType());
                //Se inicializa los estados del Stock
                consultForm.setLstReasonType(parameterService.getEntityByxNameDomain(Constant.REASON_TYPE_STOCK));
                //request.getSession().setAttribute("ListaEstadoStock",consultForm.getLstReasonType());

                //Se inicializa la lista de sucursales
                List<NpStore> lstStore = new ArrayList<NpStore>();
                request.getSession().setAttribute("listaSucursal", lstStore);
                //Se inicializa la lista de POS
                List<NpPos> lstPOS = new ArrayList<NpPos>();
                request.getSession().setAttribute("listaPos", lstPOS);

                List<SkuProductxIdRetail> lstProduct = new ArrayList<SkuProductxIdRetail>();
                request.getSession().setAttribute("listaProductos", lstProduct);

            } else {

                consultForm.setMostrarCombos(new Long(1)); // Si es uno se oculta los combos
                //Cargamos los combos de subalmacen
                INpParameterService parameterService = (INpParameterService)getInstance("NpParameterService");
                consultForm.setLstWarehouseType(parameterService.getEntityByxNameDomain(Constant.TIPOALMACEN));
                request.getSession().setAttribute("ListaAlmacen", consultForm.getLstWarehouseType());
                //Se inicializa los estados del Stock
                consultForm.setLstReasonType(parameterService.getEntityByxNameDomain(Constant.REASON_TYPE_STOCK));

                consultForm.setTxtRetailer((String)request.getSession().getAttribute("NameRetail"));
                consultForm.setTxtStore((String)request.getSession().getAttribute("NameStore"));
                consultForm.setIdStore((Long)request.getSession().getAttribute("IdStore"));
                consultForm.setIdRetail((Long)request.getSession().getAttribute("IdRetail"));

                // cargamos los puntos de venta (pos)
                NpStore store = new NpStore();
                store.setNpstoreid(consultForm.getIdStore());
                NpPos pos = new NpPos();
                pos.setNpStore(store);
                INpPosService posService = (INpPosService)getInstance("NpPosService");
                consultForm.setLstPOS(posService.getEntityByProperty(null, pos));
                request.getSession().setAttribute("listaPos", consultForm.getLstPOS());

                INpSkuProductService skuProductService = (INpSkuProductService)getInstance("NpSkuProductService");
                consultForm.setLstProduct(skuProductService.getEntityByxIdRetail(new Long(consultForm.getIdRetail())));
                request.getSession().setAttribute("listaProductos", consultForm.getLstProduct());
            }
            logger.info("Fin productStockHome");

            String flagDetalleStock = (String)request.getSession().getAttribute("detallestock");

            if (flagDetalleStock != null && flagDetalleStock.equals("1")) {
                //Eliminar el indicador para mostrar el formulario Consulta detallada productos en stock
                request.getSession().removeAttribute("detallestock");
                return mapping.findForward("SearchProductStockDetail");
            } else {
                return mapping.findForward("SearchProductStock");
            }
    }


    //Permite consultar el stock disponible

    public ActionForward productStockSearch(ActionMapping mapping, 
                                            ActionForm form, 
                                            HttpServletRequest request, 
                                            HttpServletResponse response) throws Exception {

        NpAvailableProduct availableProduct = new NpAvailableProduct();
        List<NpAvailableProduct> listaProductosStock = null;

            logger.info("Inicio productStockSearch");
            ConsultForm consultForm = (ConsultForm)form;

            INpProductService productService = 
                (INpProductService)getInstance("NpProductService");
            // LOGICA PARA BUSCAR  STOCK DISPONIBLE                        
            //cargamos las constantes para los estados
            Long subwarehouseAvailable = 
                new Long(Constant.P_SUBWAREHOUSE_AVAILABLE);
            Long subwarehouseReturned = 
                new Long(Constant.P_SUBWAREHOUSE_RETURNED);
            //CRM 19-05-09 Para mostrar los combos si es administrador
            String rol = (String)request.getSession().getAttribute("rol");
            if (rol != null && rol.equals("admin")) {
                availableProduct.setNpposid(consultForm.getCmbPOS() != null && 
                                            !consultForm.getCmbPOS().equals("") ? 
                                            new Long(consultForm.getCmbPOS()) : 
                                            0);
                availableProduct.setNpproductid(consultForm.getCmbProduct() != 
                                                null && 
                                                !consultForm.getCmbProduct().equals("") ? 
                                                new Long(consultForm.getCmbProduct()) : 
                                                0);
                availableProduct.setNpretailerid(consultForm.getCmbRetailer() != 
                                                 null && 
                                                 !consultForm.getCmbRetailer().equals("") ? 
                                                 new Long(consultForm.getCmbRetailer()) : 
                                                 0);
                availableProduct.setNpstoreid(consultForm.getCmbStore() != 
                                              null && 
                                              !consultForm.getCmbStore().equals("") ? 
                                              new Long(consultForm.getCmbStore()) : 
                                              0);
                availableProduct.setNpsubwarehouseid(new Long(consultForm.getCmbSubWarehouse()));
                availableProduct.setStatusAvailableId(subwarehouseAvailable);
                availableProduct.setStatusReturnedId(subwarehouseReturned);
            } else {
                availableProduct.setNpposid(new Long(consultForm.getCmbPOS()));
                availableProduct.setNpproductid(new Long(consultForm.getCmbProduct()));
                availableProduct.setNpretailerid(new Long(consultForm.getIdRetail()));
                availableProduct.setNpstoreid(new Long(consultForm.getIdStore()));
                availableProduct.setNpsubwarehouseid(new Long(consultForm.getCmbSubWarehouse()));
                availableProduct.setStatusAvailableId(subwarehouseAvailable);
                availableProduct.setStatusReturnedId(subwarehouseReturned);
            }

            listaProductosStock = 
                    productService.getAvailableProducts(availableProduct);

            request.getSession().setAttribute("listaProductosStock", 
                                              listaProductosStock);

            //CRM 27-05-09 PARA QUE CARGE LOS COMBOS SUCURSAL 
            NpRetailer retail = new NpRetailer();
            if (request.getParameter("cmbRetailer") != null && 
                !request.getParameter("cmbRetailer").equals(""))
                retail.setNpretailerid(new Long(request.getParameter("cmbRetailer")));
            else
                retail.setNpretailerid(null);
            NpStore store = new NpStore();
            store.setNpRetailer(retail);
            //Traemos las sucursales que pertenecen a una Cadena       
            INpStoreService storeService = 
                (INpStoreService)getInstance("NpStoreService");
            List<NpStore> listaStore = 
                storeService.getEntityByProperty(null, store);
            request.getSession().setAttribute("listaSucursal", listaStore);

            //*************
            //productosResponse.getAT_AVAILABLEPRODUCTS_LST();
            logger.info("Fin productStockSearch");
            return mapping.findForward("SearchProductStock");
    }

    //Permite inicializar las varibales
    public ActionForward saleHome(ActionMapping mapping, ActionForm form, 
                                  HttpServletRequest request, 
                                  HttpServletResponse response) throws Exception {

            logger.info("Inicio saleHome");
            ConsultForm consultForm = (ConsultForm)form;
            //CRM - 08-05-09 Se limpia las variables
            consultForm.setCmbRetailer("");
            consultForm.setCmbStore("");
            consultForm.setCmbPOS("");
            consultForm.setCmbPromoter("");
            consultForm.setCmbOrderStatus("");
            consultForm.setCmbOrderType("");
            consultForm.setCmbSolution("");
            request.getSession().removeAttribute("consultSaleSearch");
            //*************
            consultForm.setTxtDateEnd(DateUtils.fechaActualFormatoDDMMYYYY());
            consultForm.setTxtDateHome(DateUtils.fechaActualFormatoDDMMYYYY());
            INpParameterService parameterService = 
                (INpParameterService)getInstance("NpParameterService");
            INpOrderService orderService = 
                (INpOrderService)getInstance("NpOrderService");
            //Cargamos el combo Estado de la Orden
            consultForm.setLstOrderStatus(parameterService.getEntityByxNameDomain(Constant.ESTADOORDEN));
            request.getSession().setAttribute("ListaEstadoOrden", 
                                              consultForm.getLstOrderStatus());
            //Cargamos el combo Tipo de Solucion
            NpSolution s = 
                new NpSolution(); /*parameterService.getEntityByxNameDomain(Constant.TIPOSOLUCION)*/
                consultForm.setLstSolution(orderService.getSolution(s));
            request.getSession().setAttribute("ListaTipoSolucion", 
                                              consultForm.getLstSolution());
            //Cargamos el combo Tipo de Orden
            consultForm.setLstOrderType(parameterService.getEntityByxNameDomain(Constant.TIPOORDEN));
            request.getSession().setAttribute("ListaTipoOrden", 
                                              consultForm.getLstOrderType());
            //Se carga el combo Cadena
            INpRetailerService retailService = 
                (INpRetailerService)getInstance("NpRetailerService");
            consultForm.setLstRetail(retailService.getEntities(null));
            request.getSession().setAttribute("ListaCadena", 
                                              consultForm.getLstRetail());
            //Combo Sucursales
            List<NpStore> listaStore = new ArrayList<NpStore>();
            request.getSession().setAttribute("listaSucursal", listaStore);

            //Combo POS
            List<NpPos> listaPos = new ArrayList<NpPos>();
            request.getSession().setAttribute("listaPos", listaPos);

            //Combos Promotor
            List<NpPromoter> listaPromotor = new ArrayList<NpPromoter>();
            request.getSession().setAttribute("listaPromotor", listaPromotor);

            logger.info("Fin saleHome");
            return mapping.findForward("SaleSearch");
    }

    //LEL 19-08-09: Permite inicializar las varibales

    public ActionForward attendanceHome(ActionMapping mapping, ActionForm form, 
                                        HttpServletRequest request, 
                                        HttpServletResponse response) throws Exception {

            logger.info("Inicio attendanceHome");
            ConsultForm consultForm = (ConsultForm)form;

            consultForm.setCmbRetailer("");
            consultForm.setCmbStore("");
            consultForm.setCmbPOS("");
            consultForm.setCmbPromoter("");

            request.getSession().removeAttribute("attendanceSearch");

            consultForm.setTxtDateEnd(DateUtils.fechaHoraActualFormatoDDMMYYYYHHSS());
            consultForm.setTxtDateHome(DateUtils.fechaHoraActualFormatoDDMMYYYYHHSS());

            //Se carga el combo Cadena
            INpRetailerService retailService = 
                (INpRetailerService)getInstance("NpRetailerService");
            consultForm.setLstRetail(retailService.getEntities(null));
            request.getSession().setAttribute("ListaCadena", 
                                              consultForm.getLstRetail());
            //Combo Sucursales
            List<NpStore> listaStore = new ArrayList<NpStore>();
            request.getSession().setAttribute("listaSucursal", listaStore);
            //Combo POS
            List<NpPos> listaPos = new ArrayList<NpPos>();
            request.getSession().setAttribute("listaPos", listaPos);
            //Combos Promotor
            List<NpPromoter> listaPromotor = new ArrayList<NpPromoter>();
            request.getSession().setAttribute("listaPromotor", listaPromotor);

            logger.info("Fin attendanceHome");
            return mapping.findForward("AttendanceSearch");
    }

    //Permite realizar la busqueda de una venta detallada
    //LEL 12-08-09: Modificaciones por cambio de nivel de Solution

    public ActionForward saleSearch(ActionMapping mapping, ActionForm form, 
                                    HttpServletRequest request, 
                                    HttpServletResponse response) throws Exception {

            logger.info("Inicio saleSearch");
            ConsultForm consultForm = (ConsultForm)form;
            __soap_WSAP_GetSales_GetSales_pptClient client = 
                new __soap_WSAP_GetSales_GetSales_pptClient();
            GetSalesRequest soap_request = new GetSalesRequest();

            soap_request.setAD_FROMDATE(DateUtils.convertFechaFormato(consultForm.getTxtDateHome()));
            soap_request.setAD_TODATE(DateUtils.convertFechaFormato(consultForm.getTxtDateEnd()));
            soap_request.setAN_NPORDERSTATUS(!consultForm.getCmbOrderStatus().equals("") ? 
                                             new Integer(consultForm.getCmbOrderStatus()) : 
                                             0);
            soap_request.setAN_NPORDERTYPE(!consultForm.getCmbOrderType().equals("") ? 
                                           new Integer(consultForm.getCmbOrderType()) : 
                                           0);
            soap_request.setAN_NPPOSID(!consultForm.getCmbPOS().equals("") ? 
                                       new Integer(consultForm.getCmbPOS()) : 
                                       0);
            soap_request.setAN_NPREGION(0);
            soap_request.setAN_NPRETAILERID(!consultForm.getCmbRetailer().equals("") ? 
                                            new Integer(consultForm.getCmbRetailer()) : 
                                            0);
            soap_request.setAN_NPSOLUTIONCODE(consultForm.getCmbSolution());
            soap_request.setAN_NPSTOREID(!consultForm.getCmbStore().equals("") ? 
                                         new Integer(consultForm.getCmbStore()) : 
                                         0);
            soap_request.setAV_NPPROMOTERID(!consultForm.getCmbPromoter().equals("") ? 
                                            new Integer(consultForm.getCmbPromoter()) : 
                                            0);
            GetSalesResponse soap_response = client.getSales(soap_request);
            RETAILNPORDER_PKG_TT_SALES_LIST1 lista = 
                soap_response.getAT_SALE_LST();
            RETAILNP_TYPES_SALE_PKG_TR_SALES2[] listaItems = 
                lista.getAT_SALE_LST_ITEM();
            ArrayList listaItemsArray = obtenerListaItems(listaItems);
            request.getSession().setAttribute("consultSaleSearch", 
                                              listaItemsArray);

            //Se carga de nuevo el combo sucursal para que no se pierda cuando sumita la pagina.
            NpRetailer retail = new NpRetailer();
            if (request.getParameter("cmbRetailer") != null && 
                !request.getParameter("cmbRetailer").equals(""))
                retail.setNpretailerid(new Long(request.getParameter("cmbRetailer")));
            else
                retail.setNpretailerid(null);
            NpStore store = new NpStore();
            store.setNpRetailer(retail);
            //Traemos las sucursales que pertenecen a una Cadena
            INpStoreService storeService = 
                (INpStoreService)getInstance("NpStoreService");
            List<NpStore> listaStore = 
                storeService.getEntityByProperty(null, store);
            request.getSession().setAttribute("listaSucursal", listaStore);
            //Se carga de nuevo el combo Punto de Venta para que no se pierda cuando sumite
            if (request.getParameter("cmbStore") != null && 
                !request.getParameter("cmbStore").equals(""))
                store.setNpstoreid(new Long(request.getParameter("cmbStore")));
            else
                store.setNpstoreid(null);

            NpPos pos = new NpPos();
            pos.setNpStore(store);
            INpPosService posService = 
                (INpPosService)getInstance("NpPosService");
            List<NpPos> listaPos = posService.getEntityByProperty(null, pos);
            request.getSession().setAttribute("listaPos", listaPos);
            // Se carga de nuevo el combo promotor para que no se pierda cuando sumite
            if (request.getParameter("cmbPOS") != null && 
                !request.getParameter("cmbPOS").equals(""))
                pos.setNpposid(new Long(request.getParameter("cmbPOS")));
            else
                pos.setNpposid(null);

            NpPromoter promoter = new NpPromoter();
            promoter.setNpPos(pos);
            INpPromoterService promotorService = 
                (INpPromoterService)getInstance("NpPromoterService");
            //stockForm.setLstPromoter(promotorService.getEntityByProperty(null,promoter));
            List<NpPromoter> listaPromotor = 
                promotorService.getPromoterxIdPos(null, promoter);
            request.getSession().setAttribute("listaPromotor", listaPromotor);

            logger.info("Fin saleSearch");
            return mapping.findForward("SaleSearch");
    }

    public ArrayList obtenerListaItems(RETAILNP_TYPES_SALE_PKG_TR_SALES2[] listaItems) {
        try {
            logger.info("Inicio obtenerListaItems");
            int k = 0;
            ArrayList listaObtenidos = new ArrayList();
            while (listaItems != null && listaItems.length > k) {
                BeanConsultSaleSearch bean = 
                    obtenerConsultSaleSearch(listaItems[k]);
                listaObtenidos.add(bean);
                k++;
            }
            logger.info("Fin obtenerListaItems");
            return listaObtenidos;
        } catch (Exception e) {
            logger.error("", e);
        }
        return null;
    }

   /* public ArrayList convertToArray(RETAILNP_TYPES_SALE_PKG_TR_CONSOLI7[] listaItems) {
        try {
            int k = 0;
            ArrayList listaObtenidos = new ArrayList();
            while (listaItems != null && listaItems.length > k) {
                BeanCosultConsolidate bean = 
                    obtenerConsultConsolidate(listaItems[k]);
                listaObtenidos.add(bean);
                k++;
            }
            logger.info("convertToArray");
            return listaObtenidos;
        } catch (Exception e) {
            logger.error("", e);
        }
        return null;
        }*/

   /* public BeanCosultConsolidate obtenerConsultConsolidate(RETAILNP_TYPES_SALE_PKG_TR_CONSOLI7 item) {
        try {
            logger.info("Inicio obtenerConsultConsolidate");
            BeanCosultConsolidate bean = new BeanCosultConsolidate();
            bean.setAV_NPITEMSCOUNT(item.getAV_NPITEMSCOUNT());
            bean.setAV_NPNAME(item.getAV_NPNAME());
            bean.setAV_NPORDERDATE(item.getAV_NPORDERDATE());
            bean.setAV_NPPRODUCTNAME(item.getAV_NPPRODUCTNAME());
            bean.setAV_NPPROMOTERNAME(item.getAV_NPPROMOTERNAME());
            return bean;
        } catch (Exception e) {
            logger.error("", e);
        }
        return null;
        }*/

    public BeanConsultSaleSearch obtenerConsultSaleSearch(RETAILNP_TYPES_SALE_PKG_TR_SALES2 item) {
            logger.info("Inicio obtenerConsultSaleSearch");
            BeanConsultSaleSearch bean = new BeanConsultSaleSearch();
            bean.setAD_NPORDERDATE(item.getAD_NPORDERDATE());
            bean.setAN_NPORDERID(item.getAN_NPORDERID());
            bean.setAN_NPORDERNUMBER(item.getAN_NPORDERNUMBER());
            bean.setAV_NPFIRSTNAME(item.getAV_NPFIRSTNAME());
            bean.setAV_NPLASTNAME1(item.getAV_NPLASTNAME1());
            bean.setAV_NPLASTNAME2(item.getAV_NPLASTNAME2());
            bean.setAV_NPNAME(item.getAV_NPNAME());
            bean.setAV_NPORDERTYPE(item.getAV_NPORDERTYPE());
            bean.setAV_NPORDERTYPENAME(item.getAV_NPORDERTYPENAME());
            bean.setAV_NPVOUCHER(item.getAV_NPVOUCHER());
            bean.setAV_NPRETAILER(item.getAV_NPRETAILER());
            bean.setAV_ORDERSTATUS(item.getAV_ORDERSTATUS());
            bean.setAV_PAYMENTTYPE(item.getAV_PAYMENTTYPE());
            bean.setAV_POS(item.getAV_POS());
            bean.setAV_PROMOTER(item.getAV_PROMOTER());
            bean.setAV_SOLUTION(item.getAV_SOLUTION());
            bean.setAV_STORE(item.getAV_STORE());
            bean.setAN_NPORDERIDMASTER(item.getAN_NPORDERIDMASTER());
            logger.info("Fin obtenerConsultSaleSearch");
            return bean;
    }

    //Permite ver el detalle de una venta cuando se selecciona de la grilla
    //LEL 07-09-09: Cambios en forma de mostrar los detalles de las devoluciones
    //JTORRESC 20-11-2009 Se modifico por el tema de mostrar el historico de IMEI
    public ActionForward saleDetail(ActionMapping mapping, ActionForm form, 
                                    HttpServletRequest request, 
                                    HttpServletResponse response) throws Exception {

            logger.info("Inicio saleDetail");
            ConsultForm consultForm = (ConsultForm)form;
            Long orderChoose = consultForm.getOrderchoose();
            Long changeEquipment = 
                Long.parseLong(Constant.P_ORDERTYPE_CHANGEEQUIP);
            Long totalDevolution = 
                Long.parseLong(Constant.P_ORDERTYPE_DEVOLUTION);
            Long ordertypecurrent = consultForm.getOrdertype();
            Long masteridcurrent = consultForm.getOrdermasterid();


            ArrayList lista = 
                (ArrayList)request.getSession().getAttribute("consultSaleSearch");
            int k = 0;
            request.getSession().setAttribute("consultSaleDetail", null);
            while (lista != null && lista.size() > k) {
                BeanConsultSaleSearch temporal = 
                    (BeanConsultSaleSearch)lista.get(k);
                if (temporal.getAN_NPORDERID() == orderChoose.intValue()) {
                    consultForm.setTxtDateSale(temporal.getAD_NPORDERDATE_Formato() + 
                                               " " + 
                                               temporal.getAD_NPORDERHOUR());
                    consultForm.setTxtRetailer(temporal.getAV_NPRETAILER());
                    consultForm.setTxtStore(temporal.getAV_STORE());
                    consultForm.setTxtPOS(temporal.getAV_POS());
                    consultForm.setTxtPrometer(temporal.getAV_PROMOTER());
                    consultForm.setTxtTypePayment(temporal.getAV_PAYMENTTYPE());
                    consultForm.setTxtNumberVoucher(temporal.getAV_NPVOUCHER());
                    consultForm.setTxtSoluction(temporal.getAV_SOLUTION());
                    consultForm.setTxtCustomerName(temporal.getAV_NPNAME());
                    consultForm.setTxtOrderNumber(temporal.getAN_NPORDERNUMBER());
                    consultForm.setTxtOrderStatus(temporal.getAV_ORDERSTATUS());
                    ArrayList listaOrdenes = null;
                    /*if(ordertypecurrent!=null && masteridcurrent!=null && ordertypecurrent>0 && masteridcurrent>0 && (changeEquipment.equals(ordertypecurrent) || totalDevolution.equals(ordertypecurrent)))
                                {
                                   listaOrdenes = obtenerOrdenesById(consultForm.getOrdermasterid().intValue());
                                }
                                else*/
                    listaOrdenes = 
                            obtenerOrdenesById(temporal.getAN_NPORDERID());
                    request.getSession().setAttribute("consultSaleDetail", 
                                                      listaOrdenes);
                }
                k++;
            }
            logger.info("Fin saleDetail");
            return mapping.findForward("SaleDetail");
    }
    
    //RPASCACIO 27-06-2014 --> Metodo para la carga de combos Tipo Documento, Tipo Cedente y Tipo Modalidad de Pago
        public ActionForward consultPreview(ActionMapping mapping, ActionForm form, 
                                        HttpServletRequest request, 
                                        HttpServletResponse response) throws Exception {

                //Begin BROJAS - 15-07-2014 - Desabilitar opcion de Reporte de visualizacion de Portabilidad
                           int iPortaFlag = new Integer(StringUtils.notNull(request.getSession().getAttribute("portaFlag"), Constant.TYPE_INTEGER));

                            if(iPortaFlag != 1){
                               request.setAttribute("MostrarAlertDeshabilitado",1);
                               return mapping.findForward("Home");
                           }
                //End BROJAS   

                logger.info("Inicio saleDetail");
                ConsultForm consultForm = (ConsultForm)form;
                INpParameterService parameterService = (INpParameterService)getInstance("NpParameterService");
                
            /*    if(consultForm.getConsultFlag()==null){
                    sessionDelete(mapping, form,request,response);
                }              
                */
                
                //Cargamos el combo tipo documento                    
                consultForm.setLstDocumentType(parameterService.getEntityByxNameDomain(Constant.TIPODOCUMENTOPORTABILIDAD));
                request.getSession().setAttribute("ListaTipoDocumento",consultForm.getLstDocumentType());
                
                //Cargamos el combo tipo de cedente                  
                 //consultForm.setLstCedenteType(parameterService.getEntityByxNameDomain(Constant.TIPOCEDENTE));
                 //Cambio Fijo Movil
                 String serviceTypeSelected= (String) request.getParameter("serviceType");
                 if(serviceTypeSelected==null || serviceTypeSelected.trim().equals("")){
                    serviceTypeSelected="1";
                 }
                 String division="1";
                 if(serviceTypeSelected.equals("2")){
                   division="8";
                 }
                 consultForm.setLstCedenteType(parameterService.getCedentesByDivision(division));
                 request.getSession().setAttribute("ListaTipoCedente",consultForm.getLstCedenteType());
                 
                //Cargamos el combo tipo modalidad de pago
                 consultForm.setLstModalidadPagoType(parameterService.getEntityByxNameDomain(Constant.TIPOMODALIDADPAGO));
                 request.getSession().setAttribute("ListaTipoModalidadPago",consultForm.getLstModalidadPagoType());
                 
                 consultForm.setTxtPhoneNumberPortability(null);
                 consultForm.setTxtFecVencimiento(null);
                 consultForm.setTxtTipoMoneda(null);
                 consultForm.setTxtMontoAdeudado(null);
                 consultForm.setTxtEstadoConsultaPrevia(null);
                 consultForm.setTxtRentaMensual(null);
                 consultForm.setTxtDocumentNumber("");
                 
                 consultForm.setTxtCustomerName("");
                 consultForm.setTxtCustomerEmail("");
                 consultForm.setTxtCustomerTelefono("");                  
                 consultForm.setCmbCedenteType("");       
                 consultForm.setCmbDocumentType("");
                 consultForm.setServiceType(serviceTypeSelected);
                logger.info("Fin saleDetail");
                return mapping.findForward("ConsultPreview");
        }
        
    public ActionForward consultPreviewReport(ActionMapping mapping, 
                                         ActionForm form, 
                                         HttpServletRequest request, 
                                         HttpServletResponse response) throws Exception {

        String perfil= (String)request.getSession().getAttribute("perfilLogueo");
        String perfilPromoterCode = (String)request.getSession().getAttribute("perfilPromoterCode");
        //perfil= "PROM";
        
        request.setAttribute("vPerfil", perfil);
        request.setAttribute("perfilPromoterCode",perfilPromoterCode);

        

                      INpParameterService parameterService = (INpParameterService)getInstance("NpParameterService");
                      ConsultForm consultForm = (ConsultForm)form;
                      consultForm.setMostrarCombos(new Long(0)); // Si es cero se muestra los combos                                                                     
                                                                                            
                   
                       consultForm.setCmbPOS("");
                       consultForm.setCmbPromoter("");             
                       consultForm.setCmbDocumentType("");
                       consultForm.setTxtDocumentNumber("");
                       consultForm.setTxtCustomerTelefono("");
                       consultForm.setCmbRetailer("");
                       consultForm.setCmbStore("");
                       consultForm.setTxtNroTelefono("");
                       
                       consultForm.setTxtDateEnd(DateUtils.fechaActualFormatoDDMMYYYY());
                       consultForm.setTxtDateHome(DateUtils.fechaActualFormatoDDMMYYYY());
                       
                      
                         INpRetailerService retailService = 
                         (INpRetailerService)getInstance("NpRetailerService");
                         consultForm.setLstRetail(retailService.getEntities(null));
                         request.getSession().setAttribute("ListaCadena", 
                                                         consultForm.getLstRetail());  
                                                             
                           //Combo Sucursales
                           List<NpStore> listaStore = new ArrayList<NpStore>();
                           request.getSession().setAttribute("listaSucursal", listaStore);

                           //Combo POS
                           List<NpPos> listaPos = new ArrayList<NpPos>();
                           request.getSession().setAttribute("listaPos", listaPos);

                           //Combos Promotor
                           List<NpPromoter> listaPromotor = new ArrayList<NpPromoter>();
                           request.getSession().setAttribute("listaPromotor", listaPromotor);
                           
                            //Cargamos el combo tipo documento                    
                            consultForm.setLstDocumentType(parameterService.getEntityByxNameDomain(Constant.TIPODOCUMENTOPORTABILIDAD));
                            request.getSession().setAttribute("ListaTipoDocumento",consultForm.getLstDocumentType());
                             
                            //PM0010719 - INICIO
                             request.setAttribute("valPerfil", null);
                             request.setAttribute("perfilDesac", null);
                              //Configuracion de parametria
                            List<NpParameter>parameterList = parameterService.getParameterByDomainNameByParameterNameVal(Constant.RETAIL_PARAMETER_SIMPLE,Constant.PARAMETER_ACTIVE_ROLES_PERMITIDOS,2);
                            int estado = 0; 
                            for (int x=0;x<parameterList.size();x++){
                              estado = new Integer(StringUtils.notNull(parameterList.get(x).getNpstatus(),Constant.TYPE_INTEGER));
                             }
                             request.getSession().setAttribute("estadoParametria", String.valueOf(estado));
                             if(estado != 0 ){
                               //N:roles no permitidos, S:roles si permitidos
                               request.getSession().setAttribute("rolesPermitidos", "N");
                               List<NpParameter> rolesPermitidos =  parameterService.getEntityByxNameDomain(Constant.DOMINIO_ROLES_PERMITIDOS);                               
                                for(int i =0; i < rolesPermitidos.size();i++){
                                  NpParameter rolPermitido = rolesPermitidos.get(i);
                                  if(perfil.equals(rolPermitido.getNpparametervalue1())){
                                    request.getSession().setAttribute("rolesPermitidos", "S");
                                  }
                                }
                             }   
                            //PM0010719 - FIN
                                                                                           
                                                                                           
                            return mapping.findForward("ConsultPreviewReport");
    }    

    public ActionForward consultReport(ActionMapping mapping, 
                                           ActionForm form, 
                                           HttpServletRequest request, 
                                           HttpServletResponse response) throws Exception {

        ConsultForm consultForm = (ConsultForm)form;
            List<NpPreviewConsult> consultPreviewList = null;
            INpProductService productService = 
                (INpProductService)getInstance("NpProductService");
            Map parametros = new HashMap();
            
            String perfil= (String)request.getSession().getAttribute("perfilLogueo");
            
           //PM0010719 - INICIO
             String estadoParametria = (String) request.getSession().getAttribute("estadoParametria");
             String rolesPermitidos = (String) request.getSession().getAttribute("rolesPermitidos");                     
           //PM0010719 - FIN
            
            if(estadoParametria.equals("0")){
            if (perfil.equals("RTLAD")) {
                    parametros.put("an_npretailerid", 
                                   new Long(StringUtils.notNull(consultForm.getCmbRetailer(), 
                                                             Constant.TYPE_INTEGER)));
                    parametros.put("an_npstoreid", 
                                   new Long(StringUtils.notNull(consultForm.getCmbStore(), 
                                                             Constant.TYPE_INTEGER)));
                    parametros.put("an_npposid", 
                                   new Long(StringUtils.notNull(consultForm.getCmbPOS(), 
                                                             Constant.TYPE_INTEGER)));
                    parametros.put("an_nppromoterid", 
                                   new Long(StringUtils.notNull(consultForm.getCmbPromoter(), 
                                                             Constant.TYPE_INTEGER)));
                    parametros.put("av_fromdate", consultForm.getTxtDateHome());
                    parametros.put("av_todate", consultForm.getTxtDateEnd());
                    parametros.put("av_profile",perfil);
                
            }else { // perfil promoter
                 parametros.put("av_fromdate", consultForm.getTxtDateHome());
                 parametros.put("av_todate", consultForm.getTxtDateEnd());
                 parametros.put("av_profile",perfil);
                 parametros.put("av_nro_telefono",consultForm.getTxtNroTelefono());

                parametros.put("an_npretailerid",request.getSession().getAttribute("IdRetail"));
                parametros.put("an_npstoreid",request.getSession().getAttribute("IdStore"));
                parametros.put("an_npposid",request.getSession().getAttribute("IdPos"));
                parametros.put("an_nppromoterid",request.getSession().getAttribute("IdPromoter"));
            }
            }else{            
              if (rolesPermitidos.equals("S")) {
                      parametros.put("an_npretailerid", 
                                     new Long(StringUtils.notNull(consultForm.getCmbRetailer(), 
                                                               Constant.TYPE_INTEGER)));
                      parametros.put("an_npstoreid", 
                                     new Long(StringUtils.notNull(consultForm.getCmbStore(), 
                                                               Constant.TYPE_INTEGER)));
                      parametros.put("an_npposid", 
                                     new Long(StringUtils.notNull(consultForm.getCmbPOS(), 
                                                               Constant.TYPE_INTEGER)));
                      parametros.put("an_nppromoterid", 
                                     new Long(StringUtils.notNull(consultForm.getCmbPromoter(), 
                                                               Constant.TYPE_INTEGER)));
                      parametros.put("av_fromdate", consultForm.getTxtDateHome());
                      parametros.put("av_todate", consultForm.getTxtDateEnd());
                      parametros.put("av_profile",perfil);
                  
              }else { // perfil promoter
                   parametros.put("av_fromdate", consultForm.getTxtDateHome());
                   parametros.put("av_todate", consultForm.getTxtDateEnd());
                   parametros.put("av_profile",perfil);
                   parametros.put("av_nro_telefono",consultForm.getTxtNroTelefono());

                  parametros.put("an_npretailerid",request.getSession().getAttribute("IdRetail"));
                  parametros.put("an_npstoreid",request.getSession().getAttribute("IdStore"));
                  parametros.put("an_npposid",request.getSession().getAttribute("IdPos"));
                  parametros.put("an_nppromoterid",request.getSession().getAttribute("IdPromoter"));
                  
                  //PM0010719 - INICIO
                  String posSession =  StringUtils.notNull(request.getSession().getAttribute("IdPos"),2);
                  String valPerfil = "0";
                  if(StringUtils.IsNullorEmpty(posSession)){
                    valPerfil = "1";
                    request.setAttribute("valPerfil", valPerfil);
                    request.setAttribute("perfilDesac", perfil);
                    return mapping.findForward("ConsultPreviewReport");
              }            
                  //PM0010719 - FIN
                  
            }
            }
                        
          //  parametros.put("an_npdocumenttype",new Integer(sUtil.notNull(consultForm.getCmbDocumentType(),
          //                                           Constant.TYPE_INTEGER)));
                                                     
            parametros.put("an_npdocumenttype",consultForm.getCmbDocumentType());                                                                
            parametros.put("an_npdocumentnumber",consultForm.getTxtDocumentNumber());                                         
                                                            
            consultPreviewList = productService.getPreviewConsultReport(parametros);
            parametros = null;
            productService = null;

            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", 
                               "attachment; filename=ReporteConsultaPrevia.xls");
            PrintWriter out = response.getWriter();
            NotNullStringBuilderDecorator sb = new NotNullStringBuilderDecorator();         



                
            sb.append("<html><style>")
            .append(".style0{mso-number-format:General;")
            .append("text-align:general; vertical-align:bottom;")
            .append("white-space:nowrap;mso-rotate:0;")
            .append("mso-background-source:auto;mso-pattern:auto;")
            .append("color:windowtext;font-size:10.0pt;")
            .append("font-weight:400;font-style:normal;")
            .append("text-decoration:none;font-family:Arial;")
            .append("mso-generic-font-family:auto;mso-font-charset:0;")
            .append("border:none;mso-protection:locked visible;")
            .append("mso-style-name:Normal;mso-style-id:0;} ")
            .append(".xl67{mso-style-parent:style0; color:black;")
            .append("font-size:8.0pt;")
            //RPASCACIO Formato de la hora 
            .append(".xl68{mso-number-format:/”Long Time/”;}")
            //RPASCACIO
            .append("font-family:sansserif;mso-generic-font-family:auto;mso-font-charset:0;")
            .append("mso-number-format:\"dd\\/mm\\/yyyy\";")
            .append("text-align:left;vertical-align:top;white-space:normal;}")
            .append(".xl72{mso-style-parent:style0;")
            .append("color:black;font-size:8.0pt;font-family:sansserif;")
            .append("mso-generic-font-family:auto;mso-font-charset:0;")
            .append("mso-number-format:Standard;text-align:right;")
            .append("vertical-align:top;white-space:normal;}")
            .append(".xl71{mso-number-format:00000000;}")
            .append(".xl81{mso-number-format:\\@;}")
            .append(".xl73{font-size:12.0pt;font-weight:bold;font-family:sansserif;}")
            .append(".x174{font-size:10.0pt;font-weight:bold;font-family:sansserif;}")
            .append("td{font-size:8.0pt;font-family:sansserif;}")
 
           
           
           .append("</style><body><table>")
           .append("<tr><td>")           
           .append(DateUtils.fechaHoraActualFormatoDDMMYYYYHHSS())
           .append("</td><td></td><td></td><td></td><td></td><td></td>")          
           .append("<td collspan=4 class=xl73>Reporte Consulta Previa</td>")
           .append("<td></td><td></td><td></td><td></td><td></td><td></td><td></td><td></td><td></td><td></td>")
           .append("<td></td><td></td><td></td><td></td><td></td><td></td><td></td></tr>")
           .append("<tr><td class=x174>Fecha de Consulta Previa</td><td class=x174>Cadena</td><td class=x174>Sucursal</td><td class=x174>Punto de Venta</td>")
           .append("<td class=x174>Promotor</td><td class=x174>Cedente</td><td class=x174>Nombres y Apellidos del Cliente</td><td class=x174>Correo del Cliente</td><td class=x174>Tipo de Documento</td>")
           .append("<td class=x174>Nro. Doc Cliente(DNI/RUC)</td><td class=x174>Telefono</td><td class=x174>Modalidad</td>" +
            "<td class=x174>Estado de Consulta Previa</td>")
           .append("<td class=x174>Motivo de Rechazo</td>")
           .append("<td class=x174>Fecha de Activaci&oacute;n</td>");
           //"<td class=x174>Monto Adeudado</td><td class=x174>Tipo Moneda</td><td class=x174>Fec.Vencimiento Ultimo Recibo</td></tr>");
        
           // INpParameterService parameterService = (INpParameterService)getInstance("NpParameterService"); 
            
            
            
    if(consultPreviewList!=null){
            for (NpPreviewConsult list: consultPreviewList) {
                //NpParameter tipoDocumento = parameterService.findById(Long.parseLong(list.getNpdocumenttype()));
                sb.append("<tr><td class=x181>")
                //.append(DateUtils.convertFecha(list.getNpdateconsult()))   
                .append(list.getNpdateconsult())   
                //.append("</td><td class=xl67>").append(list.getNporderhour())
                .append("</td><td>").append(list.getNpretailer())
                .append("</td><td>").append(list.getNpstore())
                .append("</td><td>").append(list.getNppos())
                .append("</td><td>").append(list.getNppromotor())
                 .append("</td><td>").append(list.getNpCedente())
                .append("</td><td>").append(list.getNpnombres())
          /*    .append("</td><td>").append(list.getNpapellidos())*/
                .append("</td><td>").append(list.getNpemail())
                .append("</td><td>").append(list.getNpdocumenttype())
                .append("</td><td class=xl71>").append(list.getNpdocumentnumber())
                
               
                .append("</td><td>").append(list.getNptelephonenumber())
                .append("</td><td>").append(list.getNporigen())
                .append("</td><td>").append(list.getNpEstadoConsultaPrevia())
                .append("</td><td>").append(list.getNprejectionreason())
                .append("</td><td>").append(list.getNpactivationdate())
                .append("</td></tr>");              
            }
    } 
            consultPreviewList = null;
            sb.append("</table></body></html>");

            out.print(sb.toString());
            sb = null;
            out.flush();
            out.close();
            out = null;
        
        return null;
    }
        

    public ArrayList obtenerOrdenesById(int orderId) throws Exception{
        ArrayList result = new ArrayList();
        logger.info("Inicio obtenerOrdenesById");
        WebServiceTemplate gestionVentaService = (WebServiceTemplate)ApplicationContext.getBean("gestionVentaService");
        ConsultarOrdenRequestType consultarOrdenRequest = ofConsultarOrden.createConsultarOrdenRequestType();
        consultarOrdenRequest.setIdOrden(Long.valueOf(orderId));
        
        ConsultarOrdenResponse consultarOrdenResponseType =  (ConsultarOrdenResponse)gestionVentaService.marshalSendAndReceive(consultarOrdenRequest, SoapRequestHeaderModifier.getHeader(null)); 
        String mensaje = consultarOrdenResponseType.getResponseStatus().getDescripcionRespuesta();
        List<ListaOrdenMsg> listaItems = consultarOrdenResponseType.getResponseData().getListaOrdenMsg().getOrdenMsg();
        int k = 0;
        while (listaItems != null && listaItems.size() > k) {
            List<ListaOrdenMsg> temporal = listaItems;
            if (temporal.get(k).getIdOrden() == orderId) {
                List<ItemOrdenMsgType> listaDetalleItems = temporal.get(k).getListaItemOrdenMsg().getItemOrdenMsg();
                int j = 0;
                    INpOrderItemService orderItemService=(INpOrderItemService)ApplicationContext.getBean("NpOrderItemService"); //vcedeno@soaint.com
                while (listaDetalleItems != null && listaDetalleItems.size() > j) {
                    ItemOrdenMsgType temporalDet = temporal.get(k).getListaItemOrdenMsg().getItemOrdenMsg().get(j);
                    float amount = (float)temporalDet.getMonto();
                    int productId = (int)temporalDet.getIdProducto();
                    String productName = temporalDet.getNombreProducto();
                    String sku = temporalDet.getSku();
                    String numVoucher = temporalDet.getVoucher();
                    String solutionname = temporalDet.getNombreSolucion();
                    List<ItemOrdenEquipoMsgType> listaItemsProduct = temporalDet.getListaItemOrdenEquipoMsg().getItemOrdenEquipoMsg();
                    int i = 0;
                    while (listaItemsProduct != null && listaItemsProduct.size() > i) {
                        BeanSaleDetail detalle = new BeanSaleDetail();
                        ItemOrdenEquipoMsgType item = listaItemsProduct.get(i);
                        detalle.setAN_NPAMOUNT(amount);
                        detalle.setAN_NPPRODUCTID(productId);
                        detalle.setAV_NPPRODUCTNAME(productName);
                        detalle.setAV_NPSKU(sku);
                        detalle.setAV_NPVOUCHER(numVoucher);
                        detalle.setAN_NPCHILDPRODUCTID((int)item.getIdProducto());
                        detalle.setAN_NPORDERITEMDEVICEID((int)item.getIdItemEquipo());
                            detalle.setAV_PORTABILITY_PHONE_NUMBER(orderItemService.obtenerNumeroPortado((int)item.getIdItemEquipo()));
                        detalle.setAV_NPSOLUTIONNAME(solutionname);
                        detalle.setAN_NPPLANID(Integer.parseInt(item.getIdPlan()));
                        detalle.setAV_NPCHILDPRODUCTNAME(item.getNombreProducto());
                        detalle.setAV_NPIMEINUMBER(item.getNumeroIMEI());
                        detalle.setAV_NPPLANNAME(item.getNombrePlan());
                        detalle.setAV_NPSIMNUMBER(item.getNumeroSIM());
                        detalle.setAV_PHONENUMBER(item.getNumeroTelefono());
                        detalle.setIndice(j);
                        result.add(detalle);
                        i++;
                    }
                    j++;
                }
                break;
            }
            k++;
        }
        if(logger.isDebugEnabled()){
          logger.debug("Fin obtenerOrdenesById");
        }
        return result;
    }

    //Permite inicializar las variables
    public ActionForward saleConsolidateHome(ActionMapping mapping, 
                                             ActionForm form, 
                                             HttpServletRequest request, 
                                             HttpServletResponse response) throws Exception {

            logger.info("Inicio saleConsolidateHome");
            ConsultForm consultForm = (ConsultForm)form;
            consultForm.setTxtDateEnd(DateUtils.fechaActualFormatoDDMMYYYY());
            consultForm.setTxtDateHome(DateUtils.fechaActualFormatoDDMMYYYY());
            consultForm.setTxtRetailer("");
            consultForm.setTxtStore("");
            consultForm.setTxtPOS("");
            consultForm.setCmbProduct("");
            consultForm.setCmbOrderStatus("");
            consultForm.setCmbRetailer("");
            consultForm.setCmbStore("");
            consultForm.setCmbPOS("");
            consultForm.setCmbPromoter("");
            request.getSession().setAttribute("consultSaleConsolidate", null);
            SkuProductxIdRetail listaProductos = new SkuProductxIdRetail();
            request.getSession().setAttribute("listaProductos", 
                                              listaProductos);
            INpParameterService parameterService = 
                (INpParameterService)getInstance("NpParameterService");
            //Cargamos el combo Estado de la Orden
            consultForm.setLstOrderStatus(parameterService.getEntityByxNameDomain(Constant.ESTADOORDEN));
            request.getSession().setAttribute("ListaEstadoOrden", 
                                              consultForm.getLstOrderStatus());
            consultForm.setTxtRetailer((String)request.getSession().getAttribute("NameRetail"));
            consultForm.setTxtStore((String)request.getSession().getAttribute("NameStore"));
            consultForm.setTxtPOS((String)request.getSession().getAttribute("NamePos"));
            consultForm.setIdStore((Long)request.getSession().getAttribute("IdStore"));
            consultForm.setIdRetail((Long)request.getSession().getAttribute("IdRetail"));
            consultForm.setIdPos((Long)request.getSession().getAttribute("IdPos"));

            consultForm.setCmbPOS("" + 
                                  (Long)request.getSession().getAttribute("IdPos"));
            consultForm.setCmbRetailer("" + 
                                       (Long)request.getSession().getAttribute("IdRetail"));
            consultForm.setCmbStore("" + 
                                    (Long)request.getSession().getAttribute("IdStore"));

            consultForm.setLstPromoter((List)request.getSession().getAttribute("ListaPromotores"));
            request.getSession().setAttribute("ListaPromotores", 
                                              consultForm.getLstPromoter());

            String rol = (String)request.getSession().getAttribute("rol");
            if (rol != null && rol.equals("admin")) {
                consultForm.setLstProduct(null);
                request.getSession().setAttribute("listaProductos", 
                                                  consultForm.getLstProduct());
            } else {
                INpSkuProductService skuProductService = 
                    (INpSkuProductService)getInstance("NpSkuProductService");
                consultForm.setLstProduct(skuProductService.getEntityByxIdRetailReport(new Long(consultForm.getIdRetail())));
                request.getSession().setAttribute("listaProductos", 
                                                  consultForm.getLstProduct());
                request.getSession().setAttribute("listaPromotor", 
                                                  consultForm.getLstPromoter());
            }
            ///***************

            INpRetailerService retailService = 
                (INpRetailerService)getInstance("NpRetailerService");
            consultForm.setLstRetail(retailService.getEntities(null));
            request.getSession().setAttribute("ListaCadena", 
                                              consultForm.getLstRetail());

            logger.info("Fin saleConsolidateHome");
            return mapping.findForward("SaleConsolidate");
    }

    public List<SkuProductxIdRetail> obtenerSkuProducts(List<NpProduct> listaProducts) {
        logger.info("Inicio obtenerSkuProducts");
        int k = 0;
        List<SkuProductxIdRetail> listaResult = 
            new ArrayList(listaProducts.size());
        while (listaProducts != null && listaProducts.size() > k) {
            SkuProductxIdRetail temporal = new SkuProductxIdRetail();
            NpProduct temporalAux = listaProducts.get(k);
            temporal.setProductId(temporalAux.getNpproductid());
            temporal.setProductName(temporalAux.getNpproductname());
            listaResult.add(temporal);
            k++;
        }
        logger.info("Fin obtenerSkuProducts");
        return listaResult;
    }
    
    //Permite realizar la consulta consolidad de ventas
    public ActionForward saleConsolidate(ActionMapping mapping, 
                                         ActionForm form, 
                                         HttpServletRequest request, 
                                         HttpServletResponse response) throws Exception {

            logger.info("Inicio saleConsolidate");
            ConsultForm consultForm = (ConsultForm)form;
                
            DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
            Date startDate=df.parse(consultForm.getTxtDateHome());
            Date endDate=df.parse(consultForm.getTxtDateEnd());    
                
            INpOrderItemService iNpOrderItemService = (INpOrderItemService) getInstance("NpOrderItemService"); //lvalencia
            List<NpOrder> listaItems = iNpOrderItemService.getConsolidatedSales(
                                       consultForm.getCmbRetailer() != null && !consultForm.getCmbRetailer().equals("") ? new Integer(consultForm.getCmbRetailer()) : 0,
                                       consultForm.getCmbStore() != null && !consultForm.getCmbStore().equals("") ? new Integer(consultForm.getCmbStore()) : 0,
                                       consultForm.getCmbPOS() != null && !consultForm.getCmbPOS().equals("") ? new Integer(consultForm.getCmbPOS()) : 0,
                                       consultForm.getCmbPromoter() != null && !consultForm.getCmbPromoter().equals("") ? new Integer(consultForm.getCmbPromoter()) : 0,
                                       consultForm.getCmbProduct() != null && !consultForm.getCmbProduct().equals("") ? new Integer(consultForm.getCmbProduct()) : 0,
                                       startDate, endDate, consultForm.getCmbOrderStatus() != null && !consultForm.getCmbOrderStatus().equals("") ? consultForm.getCmbOrderStatus() : "0"); //lvalencia    
                
            request.getSession().setAttribute("consultSaleConsolidate", listaItems);

            logger.info("Fin saleConsolidate");
            return mapping.findForward("SaleConsolidate");
    }

    //Permite inicializar las variables
    public ActionForward saleDetailHome(ActionMapping mapping, ActionForm form, 
                                        HttpServletRequest request, 
                                        HttpServletResponse response) throws Exception {

            logger.info("Inicio saleDetailHome");
                                    
        INpParameterService parameterServiceParameter = (INpParameterService)getInstance("NpParameterService");
        List<NpParameter> listParametros = parameterServiceParameter.getEntityByxNameDomain(Constant.DOMINIO_RANGO_FECHAS);
        int rangFechas = 0;
        if(listParametros!=null && !listParametros.isEmpty()){
            for(int i=0; i<listParametros.size(); i++){
               NpParameter paramEval = listParametros.get(i);
               if(paramEval.getNpparametername().equals(Constant.PARAM_RANGO_CONSULTA_DET)){
                  try{
                    rangFechas = Integer.parseInt(paramEval.getNpparametervalue1());
                  }catch(NumberFormatException e){
                      rangFechas =0;
                  }
                  break;
               }
            }
        }
        request.getSession().setAttribute("rangFechas",rangFechas);
        
        boolean isPromoter=false;
        String rolLogueado= request.getSession().getAttribute("rol").toString();
        String perfilPromoterCode= request.getSession().getAttribute("perfilPromoterCode").toString();
        Long codigoCadena = (Long)request.getSession().getAttribute("IdRetail");
        Long codigoSucursal= (Long)request.getSession().getAttribute("IdStore");
        
        
            ConsultForm consultForm = (ConsultForm)form;
            //CARGAMOS LA INFO PARA LOS COMBOS
            INpParameterService parameterService = 
                (INpParameterService)getInstance("NpParameterService");
            INpRetailerService retailService = 
                (INpRetailerService)getInstance("NpRetailerService");
          
        List<NpRetailer> listaTemp= retailService.getEntities(null);
        List<NpStore> listaStore = new ArrayList<NpStore>();
        List<NpPos> listaPos = new ArrayList<NpPos>();
        
        if(rolLogueado.equals(perfilPromoterCode)) {
            isPromoter=true;
            List<NpRetailer> listaRetail=new ArrayList<NpRetailer>();
            for(NpRetailer ret: listaTemp){
               if(ret.getNpretailerid().equals(codigoCadena)){
                    listaRetail.add(ret);
                }
            }
            consultForm.setLstRetail(listaRetail);  
            //Cargar store
            INpStoreService storeService = (INpStoreService)getInstance("NpStoreService");
            NpRetailer retail = new NpRetailer();
            retail.setNpretailerid(new Long(codigoCadena));//
            NpStore store = new NpStore();
            store.setNpRetailer(retail);
            listaStore = storeService.getEntityByProperty(null,store);
            
            List<NpStore> listaStorePromoter = new ArrayList<NpStore>();
            for(NpStore str: listaStore){
               if(str.getNpstoreid().equals(codigoSucursal)){
                    listaStorePromoter.add(str);
                }
            }
            request.getSession().setAttribute("listaSucursal", listaStorePromoter);
            
            //Combo POS
            INpPosService posService = (INpPosService)getInstance("NpPosService");
            NpPos pos = new NpPos();
            pos.setNpStore(store);
            store.setNpstoreid(new Long(codigoSucursal));
             listaPos = posService.getEntityByProperty(null,pos);
           
            List<NpPos> listaPosPromoter = new ArrayList<NpPos>();
            for(NpPos pospromoter: listaPosPromoter){
               if(pospromoter.getNpposid().equals(codigoSucursal)){
                    listaPosPromoter.add(pospromoter);
                }
            }
            request.getSession().setAttribute("listaPos", listaPosPromoter);
            
        }else{  // si no es promoter
            consultForm.setLstRetail(listaTemp);
            request.getSession().setAttribute("listaSucursal", listaStore);
            request.getSession().setAttribute("listaPos", listaPos);
        }
        
            request.getSession().setAttribute("ListaCadena", 
                                              consultForm.getLstRetail());
            request.getSession().setAttribute("isPromoter",isPromoter);
        
            request.getSession().setAttribute("listaPos", listaPos);
            //Combos Promotor
            List<NpPromoter> listaPromotor = new ArrayList<NpPromoter>();
            request.getSession().setAttribute("listaPromotor", listaPromotor);
            //Cargamos el combo Estado de la Orden
            consultForm.setLstOrderStatus(parameterService.getEntityByxNameDomain(Constant.ESTADOORDEN));
            request.getSession().setAttribute("ListaEstadoOrden", 
                                              consultForm.getLstOrderStatus());
            //Cargamos el combo Tipo de Solucion
            NpSolution s = new NpSolution();
            INpOrderService orderService = 
                (INpOrderService)getInstance("NpOrderService"); /*parameterService.getEntityByxNameDomain(Constant.TIPOSOLUCION)*/
                consultForm.setLstSolution(orderService.getSolution(s));
            request.getSession().setAttribute("ListaTipoSolucion", 
                                              consultForm.getLstSolution());
            //Cargamos el combo Tipo de Orden
            consultForm.setLstOrderType(parameterService.getEntityByxNameDomain(Constant.TIPOORDEN));
            request.getSession().setAttribute("ListaTipoOrden", 
                                              consultForm.getLstOrderType());

            consultForm.setTxtDateEnd(DateUtils.fechaActualFormatoDDMMYYYY());
            consultForm.setTxtDateHome(DateUtils.fechaActualFormatoDDMMYYYY());
            consultForm.setCmbPOS("");
            consultForm.setCmbRetailer("");
            consultForm.setCmbStore("");
            consultForm.setCmbSolution("");
            consultForm.setCmbOrderStatus("");
            consultForm.setCmbOrderType("");

            //CRM 08-05-09 Se limpia la lista
            request.getSession().removeAttribute("listaVentasDetalle");
            //****
            logger.info("Fin saleDetailHome");
            return mapping.findForward("SaleDetailSearch");
    }

    //Permite realizar una buesqueda de una venta detallada 
    public ActionForward saleDetailSearch(ActionMapping mapping, 
                                          ActionForm form, 
                                          HttpServletRequest request, 
                                          HttpServletResponse response) throws Exception {

            logger.info("Inicio saleDetailSearch");
            ConsultForm consultForm = (ConsultForm)form;

            Integer retailerId=null, storeId=null, posId=null, promoterId=null, orderType=null, orderStatus=null, solutionCode=null;
            
            if ((consultForm.getCmbRetailer() != null) && 
                (!consultForm.getCmbRetailer().equals(""))) {
                retailerId = new Integer(consultForm.getCmbRetailer());
            }

            if ((consultForm.getCmbStore() != null) && 
                (!consultForm.getCmbStore().equals(""))) {
                storeId = new Integer(consultForm.getCmbStore());
            }

            if ((consultForm.getCmbPOS() != null) && 
                (!consultForm.getCmbPOS().equals(""))) {
                posId = new Integer(consultForm.getCmbPOS());
            }

            if ((consultForm.getCmbPromoter() != null) && 
                (!consultForm.getCmbPromoter().equals(""))) {
                promoterId = new Integer(consultForm.getCmbPromoter());
            }

            if ((consultForm.getCmbOrderStatus() != null) && 
                (!consultForm.getCmbOrderStatus().equals(""))) {
                orderStatus = new Integer(consultForm.getCmbOrderStatus());
            }

            if ((consultForm.getCmbOrderType() != null) && 
                (!consultForm.getCmbOrderType().equals(""))) {
                orderType = new Integer(consultForm.getCmbOrderType());
            }

            if ((consultForm.getCmbSolution() != null) && 
                (!consultForm.getCmbSolution().equals(""))) {
                solutionCode = new Integer(consultForm.getCmbSolution());
            }
            
            DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
            Date startDate=df.parse(consultForm.getTxtDateHome());
            Date endDate=df.parse(consultForm.getTxtDateEnd()); 
                
            INpOrderItemService iNpOrderItemService = (INpOrderItemService) getInstance("NpOrderItemService"); //lvalencia
            List<SaleBean> saleDetailLst = iNpOrderItemService.getSaleDetailList(retailerId, storeId, posId, promoterId, startDate,
                                                                                 endDate, orderType, orderStatus, solutionCode);    

            List<OrderDetailConsult> detalleTemp = new ArrayList<OrderDetailConsult>();
            if (saleDetailLst != null && saleDetailLst.size() > 0) { //llenamos la lista con el resultado de la consulta
                for (int i = 0; i < saleDetailLst.size(); i++) {
                    int sw = 0;                   
                    if (saleDetailLst.get(i).getSaleDetail() != null && saleDetailLst.get(i).getSaleDetail().size() > 0) {
                        for (int j = 0; j < saleDetailLst.get(i).getSaleDetail().size(); j++) {
                            OrderDetailConsult detalle = new OrderDetailConsult();                          
                            if (sw == 0) {
                                detalle.setSolucion(saleDetailLst.get(i).getSolution());
                                detalle.setNumOrden(saleDetailLst.get(i).getOrderNumber());                                 
                                detalle.setCliente(saleDetailLst.get(i).getName());
                                
                                if (saleDetailLst.get(i).getOrderDate()!= null){
                                    detalle.setFechaVenta(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(saleDetailLst.get(i).getOrderDate()));
                                }else{
                                    detalle.setFechaVenta("");
                                }
                                
                                sw = 1;
                            } else {
                                detalle.setSolucion("");
                                detalle.setNumOrden(""); 
                                detalle.setCliente("");
                                detalle.setFechaVenta("");
                            }
                            detalle.setImei(saleDetailLst.get(i).getSaleDetail().get(j).getImeiNumber());
                            detalle.setSim(saleDetailLst.get(i).getSaleDetail().get(j).getSimNumber());
                            detalle.setPhoneNumber(saleDetailLst.get(i).getSaleDetail().get(j).getPhoneNumber());
                            detalle.setPlan(saleDetailLst.get(i).getSaleDetail().get(j).getPlanName());
                            detalle.setCodProducto(saleDetailLst.get(i).getProductName());
                            detalle.setVoucher(saleDetailLst.get(i).getVoucher());
                            detalle.setTipoOrden(saleDetailLst.get(i).getOrderType());
                            detalle.setTipoPago(saleDetailLst.get(i).getPaymentType());
                            detalle.setPrecioProducto(String.valueOf(saleDetailLst.get(i).getCostProd()));
                            
                            // orderId
                            detalle.setOrderid(Integer.toString(saleDetailLst.get(i).getOrderId()));
                            
                            detalleTemp.add(detalle);
                        }
                    }
                }
            }

            request.getSession().setAttribute("listaVentasDetalle", 
                                              detalleTemp);
            //Se carga de nuevo el combo sucursal para que no se pierda cuando sumita la pagina.
            NpRetailer retail = new NpRetailer();
            if (request.getParameter("cmbRetailer") != null && 
                !request.getParameter("cmbRetailer").equals(""))
                retail.setNpretailerid(new Long(request.getParameter("cmbRetailer")));
            else
                retail.setNpretailerid(null);
            NpStore store = new NpStore();
            store.setNpRetailer(retail);
            //Traemos las sucursales que pertenecen a una Cadena
            INpStoreService storeService = 
                (INpStoreService)getInstance("NpStoreService");
            List<NpStore> listaStore = 
                storeService.getEntityByProperty(null, store);
            request.getSession().setAttribute("listaSucursal", listaStore);
            //Se carga de nuevo el combo Punto de Venta para que no se pierda cuando sumite
            if (request.getParameter("cmbStore") != null && 
                !request.getParameter("cmbStore").equals(""))
                store.setNpstoreid(new Long(request.getParameter("cmbStore")));
            else
                store.setNpstoreid(null);

            NpPos pos = new NpPos();
            pos.setNpStore(store);
            INpPosService posService = 
                (INpPosService)getInstance("NpPosService");
            List<NpPos> listaPos = posService.getEntityByProperty(null, pos);
            request.getSession().setAttribute("listaPos", listaPos);
            // Se carga de nuevo el combo promotor para que no se pierda cuando sumite
            if (request.getParameter("cmbPOS") != null && 
                !request.getParameter("cmbPOS").equals(""))
                pos.setNpposid(new Long(request.getParameter("cmbPOS")));
            else
                pos.setNpposid(null);

            NpPromoter promoter = new NpPromoter();
            promoter.setNpPos(pos);
            INpPromoterService promotorService = 
                (INpPromoterService)getInstance("NpPromoterService");
            //stockForm.setLstPromoter(promotorService.getEntityByProperty(null,promoter));
            List<NpPromoter> listaPromotor = 
                promotorService.getPromoterxIdPos(null, promoter);
            request.getSession().setAttribute("listaPromotor", listaPromotor);
            logger.info("Fin saleDetailSearch");
            return mapping.findForward("SaleDetailSearch");
    }

    //Permite inicializar las variables
    public ActionForward evaluationHome(ActionMapping mapping, ActionForm form, 
                                        HttpServletRequest request, 
                                        HttpServletResponse response) throws Exception {

            logger.info("Inicio evaluationHome");
            ConsultForm consultForm = (ConsultForm)form;
            consultForm.setTxtDateEnd(DateUtils.fechaActualFormatoDDMMYYYY());
            consultForm.setTxtDateHome(DateUtils.fechaActualFormatoDDMMYYYY());
            request.getSession().removeAttribute("listaConsolidatedEval");
            //Se carga el combo Cadena
            INpRetailerService retailService = 
                (INpRetailerService)getInstance("NpRetailerService");
            consultForm.setLstRetail(retailService.getEntities(null));
            request.getSession().setAttribute("ListaCadena", 
                                              consultForm.getLstRetail());
            //Combo Sucursales
            List<NpStore> listaStore = new ArrayList<NpStore>();
            request.getSession().setAttribute("listaSucursal", listaStore);

            //Combo POS
            List<NpPos> listaPos = new ArrayList<NpPos>();
            request.getSession().setAttribute("listaPos", listaPos);

            logger.info("Fin evaluationHome");
            return mapping.findForward("EvaluationSearch");
    }

    //Permite realizar una buesqueda de una evaluación
    //LEL 22-12-09: Cambios para que busque un estado mas: Condicionado
    public ActionForward evaluationSearch(ActionMapping mapping, 
                                          ActionForm form, 
                                          HttpServletRequest request, 
                                          HttpServletResponse response) throws Exception {

            logger.info("Inicio evaluationSearch");
            ConsultForm consultForm = (ConsultForm)form;

            //LEL 22-12-09: Para sacar de las constantes
            Integer evaluation_accept = 
                Integer.parseInt(Constant.P_ACCEPT);
            Integer evaluation_refused = 
                Integer.parseInt(Constant.P_REFUSED);
            Integer evaluation_conditioned = 
                Integer.parseInt(Constant.P_CONDITIONED);

            //Logica para la busqueda
            DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
            Date startDate=null, endDate=null;
            Integer retailerId=0, storeId=0, posId=0;
            if ((consultForm.getTxtDateHome() != null) && !consultForm.getTxtDateHome().trim().equals(""))
                startDate = df.parse(consultForm.getTxtDateHome());
            if ((consultForm.getTxtDateEnd() != null) && !consultForm.getTxtDateEnd().trim().equals(""))
                endDate = df.parse(consultForm.getTxtDateEnd());
            if (consultForm.getCmbRetailer() != null && !consultForm.getCmbRetailer().equals(""))
                retailerId = new Integer(consultForm.getCmbRetailer());
            if (consultForm.getCmbStore() != null && !consultForm.getCmbStore().equals(""))
                storeId = new Integer(consultForm.getCmbStore());
            if (consultForm.getCmbPOS() != null && !consultForm.getCmbPOS().equals(""))
                posId = new Integer(consultForm.getCmbPOS());
 
            INpEvaluationHistoryService iNpEvaluationHistoryService = (INpEvaluationHistoryService) getInstance("NpEvaluationHistoryService"); //lvalencia
            List<NpConsolidatedEvaluation> evaluationLst = iNpEvaluationHistoryService.getConsolidatedEvaluation(posId,retailerId,storeId,startDate,endDate); //lvalencia
        
            int size = evaluationLst.size();
            int cont = 0;
            boolean flag = true;
            for (int x = 0; x < size; x++) {
                //Verificar si POS ya fue registrado...
                for (int y = 0; y < x; y++) {
                    String temp1 = evaluationLst.get(y).getPosname();
                    String temp2 = evaluationLst.get(x).getPosname();
                    if (temp1.equals(temp2))
                        flag = false;
                }
                if (flag) {
                    cont++;
                }
                flag = true;
            }
            NpConsolidatedEval[] consolidatedEvalList = 
                new NpConsolidatedEval[cont];
            int cont2 = 0;
            for (int i = 0; i < size; i++) {
                //Verificar si POS ya fue registrado...
                for (int n = 0; n < i; n++) {
                    String temp1 = evaluationLst.get(n).getPosname();
                    String temp2 = evaluationLst.get(i).getPosname();
                    if (temp1.equals(temp2))
                        flag = false;
                }
                if (flag) {
                    NpConsolidatedEval item = new NpConsolidatedEval();
                    item.setAV_NPRETAILERNAME(evaluationLst.get(i).getNpretailername());
                    item.setAV_NPSTORENAME(evaluationLst.get(i).getNpstorename());
                    item.setAV_POSNAME(evaluationLst.get(i).getPosname());
                    for (int j = 0; j < size; j++) {
                        if (evaluationLst.get(j).getPosname().equals(evaluationLst.get(i).getPosname())) {
                            if (evaluationLst.get(j).getNpevaluationstatusid().equals(evaluation_accept))
                                item.setAN_TOTALACCEPT(evaluationLst.get(j).getTotal());
                            else if (evaluationLst.get(j).getNpevaluationstatusid().equals(evaluation_refused))
                                item.setAN_TOTALREJECT(evaluationLst.get(j).getTotal());
                            else if (evaluationLst.get(j).getNpevaluationstatusid().equals(evaluation_conditioned))
                                item.setAN_TOTALCONDITIONED(evaluationLst.get(j).getTotal());
                        }
                    }
                    consolidatedEvalList[cont2] = item;
                    cont2++;
                }
                flag = true;
            }

            consultForm.setListaConsolidatedEval(consolidatedEvalList);
            request.getSession().setAttribute("listaConsolidatedEval", 
                                              consultForm.getListaConsolidatedEval());

            logger.info("Fin evaluationSearch");
            return mapping.findForward("EvaluationSearch");
    }

    //Permite cargar las variables de inicio
    public ActionForward orderChangeHome(ActionMapping mapping, 
                                         ActionForm form, 
                                         HttpServletRequest request, 
                                         HttpServletResponse response) throws Exception {

            logger.info("Inicio orderChangeHome");
            ConsultForm consultForm = (ConsultForm)form;
            //CRM - 08-05-09 Se limpia los combos y grilla
            consultForm.setCmbRetailer("");
            consultForm.setCmbStore("");
            consultForm.setCmbPOS("");
            consultForm.setCmbReasonType("");
            request.getSession().removeAttribute("consultChangeSearch");
            //***********
            consultForm.setTxtDateHome(DateUtils.fechaActualFormatoDDMMYYYY());
            consultForm.setTxtDateEnd(DateUtils.fechaActualFormatoDDMMYYYY());
            INpParameterService parameterService = 
                (INpParameterService)getInstance("NpParameterService");
            //Cargamos el combo Estado de la Orden
            consultForm.setLstReasonType(parameterService.getEntityByxNameDomain(Constant.TIPOMOTIVO));
            request.getSession().setAttribute("ListaMotivos", 
                                              consultForm.getLstReasonType());
            //Se carga el combo Cadena
            INpRetailerService retailService = 
                (INpRetailerService)getInstance("NpRetailerService");
            consultForm.setLstRetail(retailService.getEntities(null));
            request.getSession().setAttribute("ListaCadena", 
                                              consultForm.getLstRetail());

            //Combo Sucursales
            List<NpStore> listaStore = new ArrayList<NpStore>();
            request.getSession().setAttribute("listaSucursal", listaStore);

            //Combo POS
            List<NpPos> listaPos = new ArrayList<NpPos>();
            request.getSession().setAttribute("listaPos", listaPos);

            logger.info("Fin orderChangeHome");
            return mapping.findForward("OrderChangeSearch");
    }

    //LEL 19-08-09: Nuevo método

    public ActionForward attendanceSearch(ActionMapping mapping, 
                                          ActionForm form, 
                                          HttpServletRequest request, 
                                          HttpServletResponse response) throws Exception {

        ConsultForm consultForm = (ConsultForm)form;

            logger.info("Inicio attendanceSearch");
                
            DateFormat df = new SimpleDateFormat("dd/mm/yyyy");
            Date startDate=df.parse(DateUtils.convertFechaHoraFormato(consultForm.getTxtDateHome()));
            Date endDate=df.parse(DateUtils.convertFechaHoraFormato(consultForm.getTxtDateEnd()));    
                
            INpPromoterService iNpPromoterService = (INpPromoterService) getInstance("NpPromoterService"); //lvalencia
            PromoterAttendanceBean promoterAttendanceLst = iNpPromoterService.getAttendancePromoters(!consultForm.getCmbPromoter().equals("") ? new Integer(consultForm.getCmbPromoter()) : 0,
                                                                                                     !consultForm.getCmbRetailer().equals("") ? new Integer(consultForm.getCmbRetailer()) : 0,
                                                                                                     !consultForm.getCmbStore().equals("") ? new Integer(consultForm.getCmbStore()) : 0,
                                                                                                     !consultForm.getCmbPOS().equals("") ? new Integer(consultForm.getCmbPOS()) : 0,
                                                                                                     startDate, endDate); //lvalencia    

            ArrayList listaItemsAsistencia = armarListaAsistencia(promoterAttendanceLst.getEntradaList(), promoterAttendanceLst.getSalidaList());

            Collections.sort(listaItemsAsistencia);

            request.getSession().setAttribute("attendanceSearch", listaItemsAsistencia);

            //Se carga de nuevo el combo sucursal para que no se pierda cuando sumita la pagina.
            NpRetailer retail = new NpRetailer();
            if (request.getParameter("cmbRetailer") != null && 
                !request.getParameter("cmbRetailer").equals(""))
                retail.setNpretailerid(new Long(request.getParameter("cmbRetailer")));
            else
                retail.setNpretailerid(null);
            NpStore store = new NpStore();
            store.setNpRetailer(retail);
            //Traemos las sucursales que pertenecen a una Cadena
            INpStoreService storeService = 
                (INpStoreService)getInstance("NpStoreService");
            List<NpStore> listaStore = 
                storeService.getEntityByProperty(null, store);
            request.getSession().setAttribute("listaSucursal", listaStore);
            //Se carga de nuevo el combo Punto de Venta para que no se pierda cuando sumite
            if (request.getParameter("cmbStore") != null && 
                !request.getParameter("cmbStore").equals(""))
                store.setNpstoreid(new Long(request.getParameter("cmbStore")));
            else
                store.setNpstoreid(null);

            NpPos pos = new NpPos();
            pos.setNpStore(store);
            INpPosService posService = 
                (INpPosService)getInstance("NpPosService");
            List<NpPos> listaPos = posService.getEntityByProperty(null, pos);
            request.getSession().setAttribute("listaPos", listaPos);
            // Se carga de nuevo el combo promotor para que no se pierda cuando sumite
            if (request.getParameter("cmbPOS") != null && 
                !request.getParameter("cmbPOS").equals(""))
                pos.setNpposid(new Long(request.getParameter("cmbPOS")));
            else
                pos.setNpposid(null);

            NpPromoter promoter = new NpPromoter();
            promoter.setNpPos(pos);
            INpPromoterService promotorService = 
                (INpPromoterService)getInstance("NpPromoterService");
            //stockForm.setLstPromoter(promotorService.getEntityByProperty(null,promoter));
            List<NpPromoter> listaPromotor = 
                promotorService.getPromoterxIdPos(null, promoter);
            request.getSession().setAttribute("listaPromotor", listaPromotor);

            logger.info("Fin attendanceSearch");
            return mapping.findForward("AttendanceSearch");
    }

    //Permite realizar una buesqueda de una orden de cambio 
    public ActionForward orderChangeSearch(ActionMapping mapping, 
                                           ActionForm form, 
                                           HttpServletRequest request, 
                                           HttpServletResponse response) throws Exception {

        ConsultForm consultForm = (ConsultForm)form;
            logger.info("Inicio orderChangeSearch");
                                              
            DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
            Date startDate=df.parse(consultForm.getTxtDateHome());
            Date endDate=df.parse(consultForm.getTxtDateEnd());                                  
                                              
            INpProductService iNpProductService = (INpProductService) getInstance("NpProductService"); //lvalencia
            List<NpProduct> productChangedLst = iNpProductService.getChangedProducts(!consultForm.getCmbRetailer().equals("") ? new Integer(consultForm.getCmbRetailer()) : 0,
                                                                                     !consultForm.getCmbStore().equals("") ? new Integer(consultForm.getCmbStore()) : 0,
                                                                                     !consultForm.getCmbPOS().equals("") ? new Integer(consultForm.getCmbPOS()) : 0,
                                                                                     !consultForm.getCmbReasonType().equals("") ? new Integer(consultForm.getCmbReasonType()) : 0,
                                                                                     startDate,endDate); //lvalencia                                  

            request.getSession().setAttribute("consultChangeSearch", productChangedLst);
            
            logger.info("Fin orderChangeSearch");
            return mapping.findForward("OrderChangeSearch");
    }

    public ActionForward printChangeSearch(ActionMapping mapping, 
                                           ActionForm form, 
                                           HttpServletRequest request, 
                                           HttpServletResponse response) throws Exception {

            /*Tal como se muestra en el jsp
            response.setContentType("application/vnd.ms-excel");//
            response.setHeader("Content-Disposition","attachment; filename=unknown.xls");
            Tal como se muestra en el jsp*/

            /*Con el diseño que quieres
            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFSheet sheet =  wb.createSheet();
            HSSFRow row = sheet.createRow(0);
            HSSFCell cell = row.createCell((short) 0);
            cell.setCellValue("Prueba");;
            OutputStream out = response.getOutputStream();
            wb.write(out);
            out.close();
            Con el diseño que quieres*/

            return null;
    }
    
    //Permite cargar las variables de inicio
    public ActionForward productAndModelHome(ActionMapping mapping, 
                                             ActionForm form, 
                                             HttpServletRequest request, 
                                             HttpServletResponse response) throws Exception {

            logger.info("Inicio productAndModelHome");
            ConsultForm consultForm = (ConsultForm)form;
            //CRM 08-05-09 Se limpia la Grilla
            request.getSession().removeAttribute("consultChangeProductAndModel");
            //*********
            consultForm.setTxtDateHome(DateUtils.fechaActualFormatoDDMMYYYY());
            consultForm.setTxtDateEnd(DateUtils.fechaActualFormatoDDMMYYYY());
            INpParameterService parameterService = 
                (INpParameterService)getInstance("NpParameterService");
            //Cargamos el combo motivo
            consultForm.setLstReasonType(parameterService.getEntityByxNameDomain(Constant.TIPOMOTIVO));
            request.getSession().setAttribute("ListaMotivos", 
                                              consultForm.getLstReasonType());
            //Se carga el combo Cadena
            INpRetailerService retailService = 
                (INpRetailerService)getInstance("NpRetailerService");
            consultForm.setLstRetail(retailService.getEntities(null));
            request.getSession().setAttribute("ListaCadena", 
                                              consultForm.getLstRetail());
            //Combo Sucursales
            List<NpStore> listaStore = new ArrayList<NpStore>();
            request.getSession().setAttribute("listaSucursal", listaStore);

            consultForm.setTxtRetailer((String)request.getSession().getAttribute("NameRetail"));
            consultForm.setTxtStore((String)request.getSession().getAttribute("NameStore"));
            consultForm.setTxtPOS((String)request.getSession().getAttribute("NamePos"));
            consultForm.setIdStore((Long)request.getSession().getAttribute("IdStore"));
            consultForm.setIdRetail((Long)request.getSession().getAttribute("IdRetail"));
            consultForm.setIdPos((Long)request.getSession().getAttribute("IdPos"));
            consultForm.setCmbPOS("" + 
                                  (Long)request.getSession().getAttribute("IdPos"));
            consultForm.setCmbRetailer("" + 
                                       (Long)request.getSession().getAttribute("IdRetail"));
            consultForm.setCmbStore("" + 
                                    (Long)request.getSession().getAttribute("IdStore"));
            //Combo POS
            List<NpPos> listaPos = new ArrayList<NpPos>();
            request.getSession().setAttribute("listaPos", listaPos);

            logger.info("Fin productAndModelHome");
            return mapping.findForward("ProductAndModelSearch");
    }
    
    //Permite realizar una buesqueda por motivo de cambio de equipo o modelo
    public ActionForward productAndModelSearch(ActionMapping mapping, 
                                               ActionForm form, 
                                               HttpServletRequest request, 
                                               HttpServletResponse response) throws Exception {

            logger.info("Inicio productAndModelSearch");
            ConsultForm consultForm = (ConsultForm)form;
            
            DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
            Date startDate=df.parse(consultForm.getTxtDateHome());
            Date endDate=df.parse(consultForm.getTxtDateEnd());
                
            INpProductService iNpProductService = (INpProductService) getInstance("NpProductService"); //lvalencia
            List<NpProduct> listaItems = iNpProductService.getChangedProductsList(!consultForm.getCmbRetailer().equals("") ? new Integer(consultForm.getCmbRetailer()) : 0,
                                                                                  !consultForm.getCmbStore().equals("") ? new Integer(consultForm.getCmbStore()) : 0,
                                                                                  !consultForm.getCmbPOS().equals("") ? new Integer(consultForm.getCmbPOS()) : 0,
                                                                                  startDate, endDate); //lvalencia    

            request.getSession().setAttribute("consultChangeProductAndModel", 
                                              listaItems);

            logger.info("Fin productAndModelSearch");
            return mapping.findForward("ProductAndModelSearch");
    }

    // CONSULTA TRANSFERENCIA ENTRE PUNTOS DE VENTA
    //Permite cargar las variables de inicio
    public ActionForward productTransferHome(ActionMapping mapping, 
                                             ActionForm form, 
                                             HttpServletRequest request, 
                                             HttpServletResponse response) throws Exception {

            logger.info("Inicio productTransferHome");
            ConsultForm consultForm = (ConsultForm)form;
            consultForm.setTxtDateHome(DateUtils.fechaActualFormatoDDMMYYYY());
            consultForm.setTxtDateEnd(DateUtils.fechaActualFormatoDDMMYYYY());
            consultForm.setCmbRetailer("");
            consultForm.setCmbStore("");
            consultForm.setCmbPOSOri("");
            consultForm.setCmbPOSDes("");
            //Se carga el combo Cadena
            INpRetailerService retailService = 
                (INpRetailerService)getInstance("NpRetailerService");
            consultForm.setLstRetail(retailService.getEntities(null));
            request.getSession().setAttribute("ListaCadena", 
                                              consultForm.getLstRetail());
            //Combo Sucursales
            List<NpStore> listaStore = new ArrayList<NpStore>();
            request.getSession().setAttribute("listaSucursal", listaStore);
            //Combo POS
            List<NpPos> listaPos = new ArrayList<NpPos>();
            request.getSession().setAttribute("listaPos", listaPos);

            //Se Carga el combo Producto
            INpProductService productService = 
                (INpProductService)getInstance("NpProductService");
            consultForm.setLstProduct(productService.getEntities(null));
            request.getSession().setAttribute("ListaProducto", 
                                              consultForm.getLstProduct());

            //limpiamos la lista de atributos de la session 
            request.getSession().removeAttribute("listaProductosTransfer");

            logger.info("Fin productTransferHome");
            return mapping.findForward("ProductTransferSearch");
    }

    //Permite realizar una buesqueda por motivo de cambio de equipo o modelo
    public ActionForward productTransferSearch(ActionMapping mapping, 
                                               ActionForm form, 
                                               HttpServletRequest request, 
                                               HttpServletResponse response) throws Exception {

            logger.info("Inicio productTransferSearch");
            ConsultForm consultForm = (ConsultForm)form;

            //Logica para la busqueda     
            Date fromDate = null, toDate = null;
            Integer productId=0, retailerId=0, storeId=0, subwareId1=0, subwareId2=0;
            
            DateFormat df = new SimpleDateFormat("dd/MM/yyyy");

            if ((consultForm.getTxtDateHome() != null) && 
                (!consultForm.getTxtDateHome().trim().equals(""))) {
                fromDate = df.parse(consultForm.getTxtDateHome());
            }

            if ((consultForm.getTxtDateEnd() != null) && 
                (!consultForm.getTxtDateEnd().trim().equals(""))) {
                toDate = df.parse(consultForm.getTxtDateEnd());
            }

            if (consultForm.getCmbProduct() != null && 
                consultForm.getCmbProduct() != "") {
                productId = new Integer(consultForm.getCmbProduct());
            }

            if (consultForm.getCmbRetailer() != null && 
                consultForm.getCmbRetailer() != "") {
                retailerId = new Integer(consultForm.getCmbRetailer());
            }

            if (consultForm.getCmbStore() != null && 
                consultForm.getCmbStore() != "") {
                storeId = new Integer(consultForm.getCmbStore());
            }

            if (consultForm.getCmbPOSOri() != null && 
                consultForm.getCmbPOSOri() != "") {
                subwareId1 = new Integer(consultForm.getCmbPOSOri());
            }

            if (consultForm.getCmbPOSDes() != null && 
                consultForm.getCmbPOSDes() != "") {
                subwareId2 = new Integer(consultForm.getCmbPOSDes());
            }
            
            INpProductService iNpProductService = (INpProductService) getInstance("NpProductService"); //lvalencia
            List<NpProduct> transferLst = iNpProductService.getTransferBetweenPoints(retailerId,storeId,subwareId1,subwareId2,productId,fromDate,toDate); //lvalencia

            consultForm.setLstTransferenciaPos(transferLst);
            request.getSession().setAttribute("listaProductosTransfer", 
                                              consultForm.getLstTransferenciaPos());

            logger.info("Fin productTransferSearch");
            return mapping.findForward("ProductTransferSearch");
    }


    //LEL - 21-08-09 Nuevo reporte
    //Permite inicializar las variables    

    public ActionForward movementsHome(ActionMapping mapping, ActionForm form, 
                                       HttpServletRequest request, 
                                       HttpServletResponse response) throws Exception {

            logger.info("Inicio movementsHome");
            ConsultForm consultForm = (ConsultForm)form;

            consultForm.setCmbRetailer("0");
            consultForm.setCmbStore("0");
            consultForm.setCmbPOS("0");
            consultForm.setCmbProduct("0");
            consultForm.setCmbState("0");

            consultForm.setTxtDateEnd(DateUtils.fechaActualFormatoDDMMYYYY());
            consultForm.setTxtDateHome(DateUtils.fechaActualFormatoDDMMYYYY());

            //Combo Cadena
            INpRetailerService retailService = 
                (INpRetailerService)getInstance("NpRetailerService");
            consultForm.setLstRetail(retailService.getEntities(null));
            request.getSession().setAttribute("ListaCadena", 
                                              consultForm.getLstRetail());
            //Combo Sucursales
            List<NpStore> listaStore = new ArrayList<NpStore>();
            request.getSession().setAttribute("listaSucursal", listaStore);
            //Combo POS
            List<NpPos> listaPos = new ArrayList<NpPos>();
            request.getSession().setAttribute("listaPos", listaPos);
            //Combo Productos          
            INpProductService productService = 
                (INpProductService)getInstance("NpProductService");
            consultForm.setLstProductGeneral(productService.getEntities(null));
            request.getSession().setAttribute("ListaProductosGeneral", 
                                              consultForm.getLstProductGeneral());


            logger.info("Fin movementsHome");
            return mapping.findForward("MovementsSearch");
    }

    //LEL - 21-08-09 Reporte hecho con ireport
    public ActionForward movementsReport(ActionMapping mapping, 
                                         ActionForm form, 
                                         HttpServletRequest request, 
                                         HttpServletResponse response) throws Exception {

        /*ConsultForm consultForm = (ConsultForm)form;

        ConnectionDB acceso = new ConnectionDB();
        Connection conexion = null;
        try {
            conexion = acceso.getConnection();
            String reporteRuta;

            if (consultForm.getCmbState().equals("2"))
                reporteRuta = "reportes/ConsultMovementsDev.jasper";
            else if (consultForm.getCmbState().equals("1"))
                reporteRuta = "reportes/ConsultMovementsDisp.jasper";
            else
                reporteRuta = "reportes/ConsultMovements.jasper";

            File reportFile = 
                new File(getServlet().getServletContext().getRealPath("/") + 
                         reporteRuta);

            if (!reportFile.exists())
                throw new JRRuntimeException("No se encontró el reporte '" + 
                                             reporteRuta + "'");

            JasperReport jasperReport = 
                (JasperReport)JRLoader.loadObject(reportFile.getPath());

            //Para poder pasar las constantes:

            Integer.parseInt(Constant.P_MODALITY_CONSIGNACION);

            Map parametros = new HashMap();
            parametros.put("an_npretailerid", 
                           new Integer(consultForm.getCmbRetailer()));
            parametros.put("an_npstoreid", 
                           new Integer(consultForm.getCmbStore()));
            parametros.put("an_npposid", 
                           new Integer(consultForm.getCmbPOS().equals("") ? 
                                       "0" : consultForm.getCmbPOS()));
            parametros.put("ad_fromdate", consultForm.getTxtDateHome());
            parametros.put("ad_todate", consultForm.getTxtDateEnd());
            parametros.put("an_productid", 
                           new Integer(consultForm.getCmbProduct()));
            parametros.put("an_stateid", 
                           new Integer(consultForm.getCmbState()));
            parametros.put("parameter_modality_sale", 
                           Integer.parseInt(Constant.P_MODALITY_SALE));
            parametros.put("parameter_modality_consignacion", 
                           Integer.parseInt(Constant.P_MODALITY_CONSIGNACION));
            parametros.put("parameter_active", 
                           Constant.P_ACTIVE);
            parametros.put("parameter_ordertype_sale", 
                           Integer.parseInt(Constant.P_ORDERTYPE_SALE));
            parametros.put("parameter_ordertype_devolution", 
                           Integer.parseInt(Constant.P_ORDERTYPE_DEVOLUTION));
            parametros.put("parameter_type_change_model", 
                           Integer.parseInt(Constant.P_ORDERTYPE_CHANGEMODEL));
            parametros.put("parameter_subwarehouse_returned", 
                           Integer.parseInt(Constant.P_SUBWAREHOUSE_RETURNED));
            parametros.put("parameter_subwarehouse_available", 
                           Integer.parseInt(Constant.P_SUBWAREHOUSE_AVAILABLE));

            ServletOutputStream ouputStream = response.getOutputStream();

            JasperPrint jasperPrint = 
                JasperFillManager.fillReport(jasperReport, parametros, 
                                             conexion);

            JRXlsExporter exporter = new JRXlsExporter();
            exporter.setParameter(JRXlsExporterParameter.JASPER_PRINT, 
                                  jasperPrint);
            exporter.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS, 
                                  Boolean.TRUE);
            exporter.setParameter(JRXlsExporterParameter.IS_COLLAPSE_ROW_SPAN, 
                                  Boolean.TRUE);
            exporter.setParameter(JRXlsExporterParameter.IS_WHITE_PAGE_BACKGROUND, 
                                  Boolean.TRUE);
            exporter.setParameter(JRXlsExporterParameter.IS_DETECT_CELL_TYPE, 
                                  Boolean.TRUE);
            exporter.setParameter(JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET, 
                                  Boolean.FALSE);
            exporter.setParameter(JRXlsExporterParameter.OUTPUT_STREAM, 
                                  ouputStream);

            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", 
                               "attachment; filename=" + "ReporteMovimientos.xls");

            exporter.exportReport();


            ouputStream.flush();
            ouputStream.close();

            return null;
        } catch (Exception e) {
            logger.error("", e);
        } finally {
            try {
                conexion.close();
            } catch (Exception e1) {
                logger.error("", e1);
            }
        }*/
        return null;
    }

    //Reporte Liquidacion  
    //CRM   06-07-2009    NUEVO REPORTE: Permite inicializar las variables
    //YRUIZ 13-05-2014    N_O000010351, Se adicionan nuevos filtros de reporte de ventas  Retail (SupervisorNextel, Departamento y Pronvincia)
    public ActionForward liquidationHome(ActionMapping mapping, 
                                         ActionForm form, 
                                         HttpServletRequest request, 
                                         HttpServletResponse response) throws Exception {
            logger.info("Inicio liquidationHome");
                             
                        INpParameterService parameterServiceParameter = (INpParameterService)getInstance("NpParameterService");
                        List<NpParameter> listParametros = parameterServiceParameter.getEntityByxNameDomain(Constant.DOMINIO_CONSULTA_LIQUIDACIONES);
                        int fechaFinal = 0;
                        if(listParametros!=null && !listParametros.isEmpty()){
                            for(int i=0; i<listParametros.size(); i++){
                               NpParameter paramEval = listParametros.get(i);
                               if(paramEval.getNpparametername().equals(Constant.PARAM_FECHA_FINAL_DIA_ANTERIOR)){
                                  try{
                                    fechaFinal = Integer.parseInt(paramEval.getNpparametervalue1());
                                  }catch(NumberFormatException e){
                                      fechaFinal =0;
                                  }
                                  break;
                               }
                            }
                        }
                        request.getSession().setAttribute("fechaFinal",fechaFinal);
		

            ConsultForm consultForm = (ConsultForm)form;
            //CRM - 08-05-09 Se limpia las variables
            consultForm.setCmbRetailer("");
            consultForm.setCmbStore("");
            consultForm.setCmbPOS("");
            consultForm.setCmbPromoter("");
            consultForm.setCmbOrderStatus("");
            consultForm.setCmbOrderType("");
            consultForm.setCmbSolution("");
            consultForm.setCmbProduct("");
            consultForm.setCmbPlan("");
            //consultForm.setCmbEvaluation("");
            consultForm.setCmbLocalidad("");
            //YRUIZ 13/05/2014
            consultForm.setCmbNextelSupervisor("");
            consultForm.setCmbDepartment("");
            consultForm.setCmbProvince("");            
            
            request.setAttribute("countConcurrentSession","");            
            //*************
            consultForm.setTxtDateEnd(DateUtils.fechaActualFormatoDDMMYYYY_Restardia(1));
            consultForm.setTxtDateHome(DateUtils.fechaActualFormatoDDMMYYYY_Restardia(1));
            INpParameterService parameterService = 
                (INpParameterService)getInstance("NpParameterService");
                
            INpOrderService orderService = 
                (INpOrderService)getInstance("NpOrderService");
                                
            //CARGAMOS EL COMBO ZONA GEOGRAFICA
            consultForm.setLstLocalidad(parameterService.getEntityByxNameDomain(Constant.TIPOZONAGEOGRAFICA));
            request.getSession().setAttribute("ListaLocalidad", 
                                              consultForm.getLstLocalidad());
            //Cargamos el combo Estado de la Orden
            consultForm.setLstOrderStatus(parameterService.getEntityByxNameDomain(Constant.ESTADOORDEN));
            request.getSession().setAttribute("ListaEstadoOrden", 
                                              consultForm.getLstOrderStatus());
            //Cargamos el combo Tipo de Solucion
            NpSolution s = 
                new NpSolution(); /*parameterService.getEntityByxNameDomain(Constant.TIPOSOLUCION)*/
                consultForm.setLstSolution(orderService.getSolution(s));
            request.getSession().setAttribute("ListaTipoSolucion", 
                                              consultForm.getLstSolution());
            //Cargamos el combo Tipo de Orden
            consultForm.setLstOrderType(parameterService.getEntityByxNameDomain(Constant.TIPOORDEN));
            request.getSession().setAttribute("ListaTipoOrden", 
                                              consultForm.getLstOrderType());
            //Se carga el combo Cadena
            INpRetailerService retailService = 
                (INpRetailerService)getInstance("NpRetailerService");
            consultForm.setLstRetail(retailService.getEntities(null));
            request.getSession().setAttribute("ListaCadena", 
                                              consultForm.getLstRetail());
            //COMBO DE PRODUCTOS GENERAL
            INpProductService productService = 
                (INpProductService)getInstance("NpProductService");
            consultForm.setLstProductGeneral(productService.getEntities(null));
            request.getSession().setAttribute("ListaProductosGeneral", 
                                              consultForm.getLstProductGeneral());
            //Se obtiene los planes 
            INpPlanService planService = 
                (INpPlanService)getInstance("NpPlanService");
            consultForm.setLstPlan(planService.lstPlanAll());
            request.getSession().setAttribute("listaPlan", 
                                              consultForm.getLstPlan());
            //CARGAMOS EL COMBO ZONA GEOGRAFICA
            consultForm.setLstZoneGeografic(parameterService.getEntityByxNameDomain(Constant.STATUSEVALUATION));
            request.getSession().setAttribute("ListaZonaGeografica", 
                                              consultForm.getLstZoneGeografic());
            //Combo Sucursales
            List<NpStore> listaStore = new ArrayList<NpStore>();
            request.getSession().setAttribute("listaSucursal", listaStore);

            //Combo POS
            List<NpPos> listaPos = new ArrayList<NpPos>();
            request.getSession().setAttribute("listaPos", listaPos);

            //Combos Promotor
            List<NpPromoter> listaPromotor = new ArrayList<NpPromoter>();
            request.getSession().setAttribute("listaPromotor", listaPromotor);

            //Combo de Zona de venta
            consultForm.setLstSaleZone(parameterService.getEntityByxNameDomain(Constant.RETAIL_ZONA_VENTA));
            
            // FTAPIA 11/09/2014 INICIO
            // Consulta de Liquidaciones, para obtener el valor por defecto del combo de Zona de Venta
            List<?> listDefSaleZone = parameterService.getParameterByDomainNameByParameterNameVal(Constant.RETAIL_ZONA_VENTA, Constant.RETAIL_LOWERCASE,2);
            NpParameter def = (listDefSaleZone != null? (listDefSaleZone.size() > 0?((NpParameter) listDefSaleZone.get(0)): null): null);
            Long salesZoneDefaultCode = null;
            if(def != null){
                salesZoneDefaultCode = def.getNpparameterid();
            }
            request.getSession().setAttribute("salesZoneDefaultCode", salesZoneDefaultCode);
            // FTAPIA 11/09/2014 FIN
            
         List<?> listDefDays = 
            parameterService.getParameterByDomainNameByParameterNameVal(Constant.RETAIL_PARAMETER_SIMPLE, 
                                                                        Constant.MAX_DAY_REPO_LIQUI, 1);
         String maxDays = (String) (listDefDays != null? (listDefDays.size() > 0? (listDefDays.get(0)) : null) : null);
         if(maxDays == null) {
            maxDays = Constant.MAX_DAY_REPO_LIQUI_DEF_DAYS;
         }
         request.getSession().setAttribute("maxDays", maxDays);
		 
            //CARGAMOS EL COMBO SUPERVISOR NEXTEL
            consultForm.setLstNextelSupervisor(parameterService.getEntityByxNameDomain(Constant.SUPERVISORNEXTEL));
            request.getSession().setAttribute("ListaSupervisorNextel",consultForm.getLstNextelSupervisor());
            
            //CARGAMOS EL COMBO DEPARTAMENTO
            INpUbigeoService ubigeoService = (INpUbigeoService)getInstance("NpUbigeoService");
            consultForm.setLstDepartment(ubigeoService.getEntities(null));
            request.getSession().setAttribute("ListaDepartamentos",consultForm.getLstDepartment());
            
            //CARGAMOS EL COMBO Provincias
            List<NpUbigeo> listProvincia = new ArrayList<NpUbigeo>();
            request.getSession().setAttribute("listaProvincias",listProvincia);
            
            logger.info("Fin liquidationHome");
            return mapping.findForward("liquidationHome");
    }


    //******************
        // MODIFICATION HISTORY
    // Person          Date         Comments
    // ------------    ----------   -------------------------------------------
    //CRM - 08-07-09 AQUI SE UTILIZA EL METODO PARA LLAMAR AL REPORTE DESARROLLADO CON IREPORT
    //JTORRESC 11-11-2009 Se agrego la validacion que cuando es NULL ingrese un valor por defecto
    //JTORRESC 22-09-2010 Se agrega el parametro de zona de venta
    //JTORRESC 13-09-2011 Se modifico la forma de exportar el xls y de esta manera se puede consultar mas cantidad de registros.
    //                    El problema que se tenia con el IReport es que a una cantidad de registros salia un error de Over head
    //                    a pasar q se sube la memoria head de Java (creando una variable de sistema en Windows EXTRA_JAVA_PROPERTIES y asignandole un valor -Xms1024m -Xmx1024m)
    //                    solo se soluciona parcialmente, pero cuando es mas registros igual sale el error.
    //YRUIZ    13-05-2014 N_O000010351, Se adicionan nuevos filtros de reporte de ventas  Retail (SupervisorNextel, Departamento y Pronvincia)
    //YRUIZ    13-05-2014 N_O000010351, Se corrigen los filtros de reporte de ventas  Retail (Departamento y Pronvincia) para que envie como datos nulos, cuando no sean seleccionados.
    //KCARPIOT 17-07-2014 RENIEC_IDDATA10: Se modifico reporte para agregar columnas de display:
    //                    nporderhour, npdocumenttype, npverificacion, npconexion
    //                    en BD : HORA, TIPDOCNUMBER, VERIFICACION,CONEXION
    //KCARPIOT 31-07-2014 PORTA: Se agrega campos de Descripcion para Reporte de Liquidaciones
    //                    en BD:  ORIGENPORTA, ESTADOPORTA, CEDENTEPORTA   
    //KCARPIOT 12-08-2014 DEPOSITO EN GARANTIA: Se agrega campo de Descripcion del Tipo de Depósito para Reporte de Liquidaciones
    
     public ActionForward liquidationReport(ActionMapping mapping, ActionForm form,
                          HttpServletRequest request, HttpServletResponse response) throws Exception {

         ConsultForm consultForm = (ConsultForm) form;
         request.setAttribute("countConcurrentSession","");
           INpParameterService parameterService = 
               (INpParameterService)getInstance("NpParameterService");
               
           List<?> listConcurrentSession = 
              parameterService.getParameterByDomainNameByParameterNameVal(Constant.RETAIL_CONCURREN_SESSION, 
                                                                          Constant.RETAIL_SESSION_COUNT, 2);
           List<?> listMaxConcurrentSession = 
           parameterService.getParameterByDomainNameByParameterNameVal(Constant.RETAIL_CONCURREN_SESSION, 
                                                                       Constant.RETAIL_SESSION_MAX, 1);
                                                                       
           int maxConcurrentSession = Integer.parseInt((String)(listMaxConcurrentSession != null? (listMaxConcurrentSession.size() > 0? (listMaxConcurrentSession.get(0)) : null) : null));
           
           NpParameter countConcurrentSession = (listConcurrentSession != null? (listConcurrentSession.size() > 0?((NpParameter) listConcurrentSession.get(0)): null): null);
           
           if(maxConcurrentSession > 0) {                                  
               //maxDays = Constant.MAX_DAY_REPO_LIQUI_DEF_DAYS;
                if(countConcurrentSession != null) {
                    if (Integer.parseInt(countConcurrentSession.getNpparametervalue1())>=maxConcurrentSession){
                        request.setAttribute("countConcurrentSession","El Reporte se esta procesando por otro Usuario, Intentelo mas tarde");
                        return mapping.findForward("liquidationHome");
                    }
                }
                
           }

           NotNullStringBuilderDecorator sb = new NotNullStringBuilderDecorator();
           response.setContentType("application/vnd.ms-excel");
           response.setHeader("Content-Disposition", "attachment; filename=ReporteLiquidacion.xls");

           PrintWriter out = response.getWriter();

           Map parametros = new HashMap();

           parametros.put("an_npretailerid", new Integer(StringUtils.notNull(consultForm.getCmbRetailer(), Constant.TYPE_INTEGER)));
           parametros.put("an_npstoreid", new Integer(StringUtils.notNull(consultForm.getCmbStore(), Constant.TYPE_INTEGER)));
           parametros.put("an_npposid", new Integer(StringUtils.notNull(consultForm.getCmbPOS(), Constant.TYPE_INTEGER)));
           parametros.put("an_nppromoterid", new Integer(StringUtils.notNull(consultForm.getCmbPromoter(), Constant.TYPE_INTEGER)));
           parametros.put("av_fromdate", new String(StringUtils.notNull(consultForm.getTxtDateHome(), Constant.TYPE_STRING)));
           parametros.put("av_todate", new String(StringUtils.notNull(consultForm.getTxtDateEnd(), Constant.TYPE_STRING)));
           parametros.put("an_npordertype", new Integer(StringUtils.notNull(consultForm.getCmbOrderType(), Constant.TYPE_INTEGER)));
           parametros.put("an_nporderstatus", new Integer(StringUtils.notNull(consultForm.getCmbOrderStatus(), Constant.TYPE_INTEGER)));
           parametros.put("an_npsolutioncode", new Integer(StringUtils.notNull(consultForm.getCmbSolution(), Constant.TYPE_INTEGER)));
           parametros.put("an_plantarif", new Integer(StringUtils.notNull(consultForm.getCmbPlan(), Constant.TYPE_INTEGER)));
           //cstmt.setLong(10, (Integer) bean.get("an_plantarif"));
           parametros.put("an_productid", new Integer(StringUtils.notNull(consultForm.getCmbProduct(), Constant.TYPE_INTEGER)));
           parametros.put("an_locationid", new Integer(StringUtils.notNull(consultForm.getCmbLocalidad(), Constant.TYPE_INTEGER)));
           parametros.put("an_salezone", new Integer(StringUtils.notNull(consultForm.getCmbSaleZone(), Constant.TYPE_INTEGER)));
           parametros.put("an_npsupervisornextelid", consultForm.getCmbNextelSupervisor() != null && !consultForm.getCmbNextelSupervisor().equals("") ? new Integer(consultForm.getCmbNextelSupervisor()) : 0);
           parametros.put("av_npdepartmentid", consultForm.getCmbDepartment() != null && !consultForm.getCmbDepartment().equals("")? consultForm.getCmbDepartment() : null);
           parametros.put("av_npprovinceid", consultForm.getCmbProvince() != null && !consultForm.getCmbProvince().equals("")? consultForm.getCmbProvince() : null);                                                                                                                                                                     
          
           //FBERNALES 04/02/2015 CONTROL DE SESSIONES - REPORTE DE LIQUIDACIONES. actualizamos el contador de sessiones a 
           NpParameter countConcurrentSessionToUpdInc = parameterService.findById(countConcurrentSession.getNpparameterid());
           int iCountSession=Integer.parseInt(countConcurrentSession.getNpparametervalue1())+1;
           countConcurrentSessionToUpdInc.setNpparametervalue1(String.valueOf(iCountSession));
           parameterService.updateEntity(countConcurrentSessionToUpdInc);
           
           INpProductService productService = (INpProductService)getInstance("NpProductService");
           List<NpLiquidation> liquidationList = productService.getLiquidationReport(parametros);
           parametros = null;
           productService = null;

             sb.append("<html>");
             sb.append("<style>");
             sb.append(".style0{mso-number-format:General;");
             sb.append("text-align:general; vertical-align:bottom;");
             sb.append("white-space:nowrap;mso-rotate:0;");
             sb.append("mso-background-source:auto;mso-pattern:auto;");
             sb.append("color:windowtext;font-size:10.0pt;");
             sb.append("font-weight:400;font-style:normal;");
             sb.append("text-decoration:none;font-family:Arial;");
             sb.append("mso-generic-font-family:auto;mso-font-charset:0;");
             sb.append("border:none;mso-protection:locked visible;");
             sb.append("mso-style-name:Normal;mso-style-id:0;} ");
             sb.append(".xl67{mso-style-parent:style0; color:black;");
             sb.append("font-size:8.0pt;");
             sb.append("font-family:sansserif;mso-generic-font-family:auto;mso-font-charset:0;");
             sb.append("mso-number-format:\"dd\\/mm\\/yyyy\";");
             sb.append("text-align:left;vertical-align:top;white-space:normal;}");
             sb.append(".xl72{mso-style-parent:style0;");
             sb.append("color:black;font-size:8.0pt;font-family:sansserif;");
             sb.append("mso-generic-font-family:auto;mso-font-charset:0;");
             sb.append("mso-number-format:Standard;text-align:right;");
             sb.append("vertical-align:top;white-space:normal;}");
             sb.append(".xl68{mso-number-format:/”Long Time/”;}");
             sb.append(".xl71{mso-number-format:\\@;}");
             sb.append(".xl73{font-size:12.0pt;font-weight:bold;font-family:sansserif;}");
             sb.append(".x174{font-size:10.0pt;font-weight:bold;font-family:sansserif;}");
             sb.append("td{font-size:8.0pt;font-family:sansserif;}");
             sb.append("</style>");
             sb.append("<body><table>");
             sb.append("<tr><td class=xl67>");
             sb.append(DateUtils.fechaHoraActualFormatoDDMMYYYYHHSS());
             sb.append("</td>");
             sb.append("<td></td><td></td><td></td><td></td><td></td><td></td><td></td><td></td><td></td>");
             sb.append("<td></td><td></td><td></td><td></td><td></td><td></td><td collspan=4 class=xl73>Reporte de Liquidación</td>");
             sb.append("<td></td><td></td><td></td><td></td><td></td><td></td><td></td><td></td><td></td><td></td>");
             sb.append("<td></td><td></td><td></td><td></td><td></td><td></td><td></td><td></td><td></td><td></td>");
             sb.append("<td></td><td></td><td></td><td></td><td></td><td></td><td></td></tr>");
             sb.append("<tr>");
             
             sb.append("<td class=x174>").append("Fecha").append("</td>");
             sb.append("<td class=x174>").append("Hora").append("</td>");
             sb.append("<td class=x174>").append("Tipo Cadena").append("</td>");
             sb.append("<td class=x174>").append("Departamento").append("</td>");
             sb.append("<td class=x174>").append("Provincia").append("</td>");
             sb.append("<td class=x174>").append("Localidad").append("</td>"); 
             sb.append("<td class=x174>").append("Cadena").append("</td>");
             sb.append("<td class=x174>").append("Sucursal").append("</td>");
             sb.append("<td class=x174>").append("Socio de Negocio").append("</td>");
             sb.append("<td class=x174>").append("Supervisor Nextel").append("</td>");
             sb.append("<td class=x174>").append("Promotor").append("</td>");
             sb.append("<td class=x174>").append("DNI Promotor").append("</td>");
             sb.append("<td class=x174>").append("Datos Promotor").append("</td>");
             sb.append("<td class=x174>").append("Nro. Orden Venta").append("</td>");
             sb.append("<td class=x174>").append("Tipo").append("</td>");
             sb.append("<td class=x174>").append("División").append("</td>");
             sb.append("<td class=x174>").append("Solución").append("</td>");
             sb.append("<td class=x174>").append("Cod. Cliente").append("</td>");
             sb.append("<td class=x174>").append("Cliente").append("</td>");
             sb.append("<td class=x174>").append("Tipo Doc Cliente").append("</td>");
             sb.append("<td class=x174>").append("Nro. Doc Cliente(DNI/RUC)").append("</td>");
             sb.append("<td class=x174>").append("Oferta Diferenciada").append("</td>");
             sb.append("<td class=x174>").append("RUC 10 con/sin Negocio").append("</td>");
             sb.append("<td class=x174>").append("Tipo de Operacion").append("</td>");
             sb.append("<td class=x174>").append("Producto").append("</td>");
             sb.append("<td class=x174>").append("Sku").append("</td>");
             sb.append("<td class=x174>").append("Modalidad").append("</td>");
             sb.append("<td class=x174>").append("Resultado de Verificación").append("</td>");
             sb.append("<td class=x174>").append("Conexion").append("</td>");
             sb.append("<td class=x174>").append("Modelo").append("</td>");
             sb.append("<td class=x174>").append("Plan Tarifario").append("</td>");
             sb.append("<td class=x174>").append("Precio Orden").append("</td>");
             sb.append("<td class=x174>").append("Precio SKU").append("</td>");
             sb.append("<td class=x174>").append("Depósito Garantía").append("</td>");
             sb.append("<td class=x174>").append("SKU Depósito Garantía").append("</td>");
             sb.append("<td class=x174>").append("Monto Depósito Garantía").append("</td>");
             sb.append("<td class=x174>").append("Voucher Depósito Garantía").append("</td>");
             sb.append("<td class=x174>").append("Forma de Pago").append("</td>");
             sb.append("<td class=x174>").append("Nro Voucher").append("</td>");
             sb.append("<td class=x174>").append("Nro. Guía").append("</td>");
             sb.append("<td class=x174>").append("IMEI").append("</td>");
             sb.append("<td class=x174>").append("SIM").append("</td>");
             sb.append("<td class=x174>").append("Nro. Contrato").append("</td>");
             sb.append("<td class=x174>").append("Nro Teléfono").append("</td>");
             sb.append("<td class=x174>").append("Estado").append("</td>");
             sb.append("<td class=x174>").append("Evaluación Crediticia").append("</td>");
             sb.append("<td class=x174>").append("Riesgo Financiero").append("</td>");
             sb.append("<td class=x174>").append("Capac. Endeudamient").append("</td>");
             sb.append("<td class=x174>").append("Capac. de Equipos").append("</td>");
             sb.append("<td class=x174>").append("Zona de Venta").append("</td>");
             sb.append("<td class=x174>").append("Tipo de Depósito").append("</td>");
             
             sb.append("<td class=x174>").append("Teléfono a Portar").append("</td>");
             sb.append("<td class=x174>").append("Modalidad de Origen").append("</td>");
             sb.append("<td class=x174>").append("Estado Portabilidad").append("</td>");
             sb.append("<td class=x174>").append("Cedente").append("</td>");
             sb.append("<td class=x174>").append("Motivo de Rechazo Portabilidad").append("</td>");
             sb.append("<td class=x174>").append("Monto Adeudado Portabilidad").append("</td>");
             sb.append("<td class=x174>").append("Moneda").append("</td>");
             sb.append("<td class=x174>").append("Fec. Venc. Últ Fact.").append("</td>");
             sb.append("<td class=x174>").append("Fecha de Ejecución Portabilidad").append("</td>");
             
             sb.append("</tr>");

           if (liquidationList != null){
           
               for (NpLiquidation liquidBean: liquidationList) {
                   sb.append("<tr>");
                   
                   sb.append("<td class=xl67>").append(liquidBean.getNporderdate()).append("</td>");
                   sb.append("<td class=xl68>").append(liquidBean.getNporderhour()).append("</td>");
                   sb.append("<td>").append(liquidBean.getNptyperetailer()).append("</td>");
                   sb.append("<td>").append(liquidBean.getNpdepartment()).append("</td>");
                   sb.append("<td>").append(liquidBean.getNpprovince()).append("</td>");
                   sb.append("<td>").append(liquidBean.getNplocalidad()).append("</td>");
                   sb.append("<td>").append(liquidBean.getNpretailer()).append("</td>");
                   sb.append("<td>").append(liquidBean.getNpstore()).append("</td>");
                   sb.append("<td>").append(liquidBean.getNpsocionegocio()).append("</td>");
                   sb.append("<td>").append(liquidBean.getNpsupervisornextel()).append("</td>");
                   sb.append("<td>").append(liquidBean.getNppromotor()).append("</td>");
                   sb.append("<td>").append(liquidBean.getNpdnipromotor()).append("</td>");
                   sb.append("<td>").append(liquidBean.getNppromotername()).append("</td>");
                   sb.append("<td>").append(liquidBean.getNpordernumber()).append("</td>");
                   sb.append("<td>").append(liquidBean.getNpordertypename()).append("</td>");
                   sb.append("<td>").append(liquidBean.getNpdivision()).append("</td>");
                   sb.append("<td>").append(liquidBean.getNpsolution()).append("</td>");
                   sb.append("<td>").append(liquidBean.getNpcodclient()).append("</td>");
                   sb.append("<td>").append(liquidBean.getNpcliente()).append("</td>");
                   sb.append("<td class=xl71>").append(liquidBean.getNpdocumenttype()).append("</td>");
                   sb.append("<td class=xl71>").append(liquidBean.getNpdocumentnumber()).append("</td>");
                   sb.append("<td class=xl72>").append(liquidBean.getNpcustportvalue()).append("</td>");
                   sb.append("<td class=xl72>").append(liquidBean.getNpcustomerbusinness()).append("</td>");
                   sb.append("<td>").append(liquidBean.getNpTypeOperation()).append("</td>");
                   sb.append("<td>").append(liquidBean.getNpproducto()).append("</td>");                     
                   sb.append("<td class=xl71>").append(liquidBean.getNpsku()).append("</td>");
                   sb.append("<td>").append(liquidBean.getNpmodalidad()).append("</td>");
                   sb.append("<td class=xl71>").append(liquidBean.getNpverificacion()).append("</td>");
                   sb.append("<td class=xl71>").append(liquidBean.getNpconexion()).append("</td>");
                   sb.append("<td>").append(liquidBean.getNpmodelo()).append("</td>");
                   sb.append("<td>").append(liquidBean.getNpplantarif()).append("</td>");
                   sb.append("<td class=xl72>").append(liquidBean.getNppriceequip()).append("</td>");
                   sb.append("<td class=xl72>").append(liquidBean.getNppricekit()).append("</td>");
                   sb.append("<td>").append(liquidBean.getNpflagdepoguarantee()).append("</td>");
                   sb.append("<td class=xl71>").append(liquidBean.getNpskudepoguarantee()).append("</td>");
                   sb.append("<td class=xl72>").append(liquidBean.getNpcostguarantee()).append("</td>");
                   sb.append("<td>").append(liquidBean.getNpvoucherguarantee()).append("</td>");
                   sb.append("<td>").append(liquidBean.getNptypepay()).append("</td>");
                   sb.append("<td class=xl71>").append(liquidBean.getNpvoucher()).append("</td>");
                   sb.append("<td class=xl71>").append(liquidBean.getNpnroguia()).append("</td>");
                   sb.append("<td class=xl71>").append(liquidBean.getNpimei()).append("</td>");
                   sb.append("<td class=xl71>").append(liquidBean.getNpsim()).append("</td>");
                   sb.append("<td class=xl71>").append(liquidBean.getNpnrocontrato()).append("</td>");
                   sb.append("<td>").append(liquidBean.getNpnumberphone()).append("</td>");
                   sb.append("<td>").append(liquidBean.getNpstatusorder()).append("</td>");
                   sb.append("<td>").append(liquidBean.getNpstatuseval()).append("</td>");
                   sb.append("<td>").append(liquidBean.getNpriesgofinanc()).append("</td>");
                   sb.append("<td class=xl72>").append(liquidBean.getNpcapaendeu()).append("</td>");
                   sb.append("<td>").append(liquidBean.getNpcantequip()).append("</td>");
                   sb.append("<td>").append(liquidBean.getNpsalezone()).append("</td>");
                   sb.append("<td>").append(liquidBean.getNptypewarranty()).append("</td>");
                   
                  sb.append("<td>").append(liquidBean.getNpNumPorta()).append("</td>");
                  sb.append("<td>").append(liquidBean.getNpOrigenPorta()).append("</td>");
                  sb.append("<td>").append(liquidBean.getNpEstadoPorta()).append("</td>");
                  sb.append("<td>").append(liquidBean.getNpCedentePorta()).append("</td>");
                  sb.append("<td>").append(liquidBean.getNpMotivoRecPorta()).append("</td>");
                  sb.append("<td class=xl72>").append(liquidBean.getNpMontoAdeudadoPorta()).append("</td>");
                  sb.append("<td>").append(liquidBean.getNpMonedaPorta()).append("</td>");
                  sb.append("<td>").append(liquidBean.getNpFecVecUlFactPorta()).append("</td>");
                  sb.append("<td>").append(liquidBean.getNpFecEjecucionPorta()).append("</td>");                       
                   
                   sb.append("</tr>");
               }
               liquidationList = null;
           }else{
              sb.append("<tr>");
              sb.append("<td colspan=\"40\">").append("No Hay Resultados").append("</td>");
              sb.append("<td>");
          }           

           sb.append("</table></body></html>");
           
           //FBERNALES 04/02/2015 CONTROL DE SESSIONES - REPORTE DE LIQUIDACIONES. actualizamos el contador de sessiones a 
           NpParameter countConcurrentSessionToUpdDec = parameterService.findById(countConcurrentSession.getNpparameterid());
           int iCountSessionDec=Integer.parseInt(countConcurrentSessionToUpdDec.getNpparametervalue1())-1;
           countConcurrentSessionToUpdDec.setNpparametervalue1(String.valueOf(iCountSessionDec));
           parameterService.updateEntity(countConcurrentSessionToUpdDec);
           
           out.print(sb.toString());
           sb = null;
           out.flush();
           out.close();
           out = null;
           return null;
     }

   //Reporte Liquidacion  x Articulo
   //Permite inicializar las variables
    public ActionForward liquiArticleHome(ActionMapping mapping, 
                                          ActionForm form, 
                                          HttpServletRequest request, 
                                          HttpServletResponse response) throws Exception {

            logger.info("Inicio liquiArticleHome");
            ConsultForm consultForm = (ConsultForm)form;
            //CRM - 08-05-09 Se limpia las variables
            consultForm.setCmbRetailer("");
            consultForm.setCmbStore("");
            consultForm.setCmbPOS("");
            consultForm.setCmbPromoter("");
            consultForm.setCmbOrderStatus("");
            consultForm.setCmbOrderType("");
            consultForm.setCmbSolution("");
            consultForm.setCmbProduct("");
            consultForm.setCmbPlan("");
            //consultForm.setCmbEvaluation("");
            consultForm.setCmbLocalidad("");
            //request.getSession().removeAttribute("consultSaleSearch");
            //*************
            consultForm.setTxtDateEnd(DateUtils.fechaActualFormatoDDMMYYYY());
            consultForm.setTxtDateHome(DateUtils.fechaActualFormatoDDMMYYYY());
            INpParameterService parameterService = 
                (INpParameterService)getInstance("NpParameterService");
            INpOrderService orderService = 
                (INpOrderService)getInstance("NpOrderService");
            //CARGAMOS EL COMBO ZONA GEOGRAFICA
            consultForm.setLstLocalidad(parameterService.getEntityByxNameDomain(Constant.TIPOZONAGEOGRAFICA));
            request.getSession().setAttribute("ListaLocalidad", 
                                              consultForm.getLstLocalidad());
            //Cargamos el combo Estado de la Orden
            consultForm.setLstOrderStatus(parameterService.getEntityByxNameDomain(Constant.ESTADOORDEN));
            request.getSession().setAttribute("ListaEstadoOrden", 
                                              consultForm.getLstOrderStatus());
            //Cargamos el combo Tipo de Solucion
            NpSolution s = 
                new NpSolution(); /*parameterService.getEntityByxNameDomain(Constant.TIPOSOLUCION)*/
                consultForm.setLstSolution(orderService.getSolution(s));
            request.getSession().setAttribute("ListaTipoSolucion", 
                                              consultForm.getLstSolution());
            //Cargamos el combo Tipo de Orden
            consultForm.setLstOrderType(parameterService.getEntityByxNameDomain(Constant.TIPOORDEN));
            request.getSession().setAttribute("ListaTipoOrden", 
                                              consultForm.getLstOrderType());
            //Se carga el combo Cadena
            INpRetailerService retailService = 
                (INpRetailerService)getInstance("NpRetailerService");
            consultForm.setLstRetail(retailService.getEntities(null));
            request.getSession().setAttribute("ListaCadena", 
                                              consultForm.getLstRetail());
            //COMBO DE PRODUCTOS GENERAL
            INpProductService productService = 
                (INpProductService)getInstance("NpProductService");
            consultForm.setLstProductGeneral(productService.getEntities(null));
            request.getSession().setAttribute("ListaProductosGeneral", 
                                              consultForm.getLstProductGeneral());
            //Se obtiene los planes 
            INpPlanService planService = 
                (INpPlanService)getInstance("NpPlanService");
            consultForm.setLstPlan(planService.lstPlanAll());
            request.getSession().setAttribute("listaPlan", 
                                              consultForm.getLstPlan());
            //CARGAMOS EL COMBO ZONA GEOGRAFICA
            consultForm.setLstZoneGeografic(parameterService.getEntityByxNameDomain(Constant.STATUSEVALUATION));
            request.getSession().setAttribute("ListaZonaGeografica", 
                                              consultForm.getLstZoneGeografic());
            //Combo Sucursales
            List<NpStore> listaStore = new ArrayList<NpStore>();
            request.getSession().setAttribute("listaSucursal", listaStore);

            //Combo POS
            List<NpPos> listaPos = new ArrayList<NpPos>();
            request.getSession().setAttribute("listaPos", listaPos);

            //Combos Promotor
            List<NpPromoter> listaPromotor = new ArrayList<NpPromoter>();
            request.getSession().setAttribute("listaPromotor", listaPromotor);

            //Combo de Zona de venta
            consultForm.setLstSaleZone(parameterService.getEntityByxNameDomain(Constant.RETAIL_ZONA_VENTA));

            logger.info("Fin liquiArticleHome");
            return mapping.findForward("liquiArticleHome");
    }


    public ActionForward liquiArticleReport(ActionMapping mapping, 
                                            ActionForm form, 
                                            HttpServletRequest request, 
                                            HttpServletResponse response) throws Exception {

        /*ConsultForm consultForm = (ConsultForm)form;
        //accesoDao acceso = new accesoDao();
        ConnectionDB acceso = new ConnectionDB();
        Connection conexion = null;
        try {
            conexion = acceso.getConnection();


            //Comenzamo a llamar al reporte, darle formato y demás
            String reporteRuta = "reportes/ConsultLiquiArticle.jasper";


            File reportFile = 
                new File(getServlet().getServletContext().getRealPath("/") + 
                         reporteRuta);

            if (!reportFile.exists())
                throw new JRRuntimeException("No se encontró el reporte '" + 
                                             reporteRuta + "'");

            JasperReport jasperReport = 
                (JasperReport)JRLoader.loadObject(reportFile.getPath());

            Map parametros = new HashMap();

            parametros.put("an_npretailerid", 
                           new Integer(StringUtils.notNull(consultForm.getCmbRetailer(), 
                                                     Constant.TYPE_INTEGER)));
            parametros.put("an_npstoreid", 
                           new Integer(StringUtils.notNull(consultForm.getCmbStore(), 
                                                     Constant.TYPE_INTEGER)));
            parametros.put("an_npposid", 
                           new Integer(StringUtils.notNull(consultForm.getCmbPOS(), 
                                                     Constant.TYPE_INTEGER)));
            parametros.put("an_nppromoterid", 
                           new Integer(StringUtils.notNull(consultForm.getCmbPromoter(), 
                                                     Constant.TYPE_INTEGER)));
            parametros.put("ad_fromdate", consultForm.getTxtDateHome());
            parametros.put("ad_todate", consultForm.getTxtDateEnd());
            parametros.put("an_npordertype", 
                           new Integer(consultForm.getCmbOrderType()));
            parametros.put("an_nporderstatus", 
                           new Integer(consultForm.getCmbOrderStatus()));
            parametros.put("an_npsolutioncode", 
                           new Integer(consultForm.getCmbSolution()));
            parametros.put("an_planid", new Integer(consultForm.getCmbPlan()));
            //parametros.put("an_evaluationid", new Integer(consultForm.getCmbEvaluation()));
            parametros.put("an_productid", 
                           new Integer(consultForm.getCmbProduct()));
            parametros.put("an_locationid", 
                           new Integer(consultForm.getCmbLocalidad()));
            parametros.put("an_salezone", 
                           new Integer(StringUtils.notNull(consultForm.getCmbSaleZone(), 
                                                     Constant.TYPE_INTEGER)));
            ServletOutputStream ouputStream = response.getOutputStream();

            JasperPrint jasperPrint = 
                JasperFillManager.fillReport(jasperReport, parametros, 
                                             conexion);

            JRXlsExporter exporter = new JRXlsExporter();
            exporter.setParameter(JRXlsExporterParameter.JASPER_PRINT, 
                                  jasperPrint);
            exporter.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS, 
                                  Boolean.TRUE);
            //LEL 18-09-09: Para que salgan mejor formateadas las filas
            exporter.setParameter(JRXlsExporterParameter.IS_COLLAPSE_ROW_SPAN, 
                                  Boolean.TRUE);

            exporter.setParameter(JRXlsExporterParameter.IS_WHITE_PAGE_BACKGROUND, 
                                  Boolean.TRUE);
            exporter.setParameter(JRXlsExporterParameter.IS_DETECT_CELL_TYPE, 
                                  Boolean.TRUE);
            exporter.setParameter(JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET, 
                                  Boolean.FALSE);
            exporter.setParameter(JRXlsExporterParameter.OUTPUT_STREAM, 
                                  ouputStream);

            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", 
                               "attachment; filename=" + "ReporteLiquiArticle.xls");

            exporter.exportReport();


            ouputStream.flush();
            ouputStream.close();

            return null;
        } catch (Exception e) {
            logger.error("", e);
        } finally {
            try {
                conexion.close();
            } catch (Exception e1) {
                logger.error("", e1);
            }
        }*/
        return null;
    }
    
    //LEL 19-08-09
    /*private ArrayList armarListaAsistencia(RETAILNP_TYPES_ATTENDANCE_PKG_TR_A4[] listaEntradasItems, 
                                           RETAILNP_TYPES_ATTENDANCE_PKG_TR_A4[] listaSalidasItems) {
        ArrayList listaFinal = new ArrayList();

        for (int i = 0; i < listaEntradasItems.length; i++) {
            BeanConsultAttendanceSearch itemAsistencia = 
                new BeanConsultAttendanceSearch();

            itemAsistencia.setAD_NPDATE(listaEntradasItems[i].getAD_ENTRYDATE());
            itemAsistencia.setAV_NPMOVTYPE("Ingreso");
            itemAsistencia.setAV_NPPOSNAME(listaEntradasItems[i].getAV_NPPOSNAME());
            itemAsistencia.setAV_NPPROMOTERNAME(listaEntradasItems[i].getAV_NPPROMOTERNAME());
            itemAsistencia.setAV_NPRETAILERNAME(listaEntradasItems[i].getAV_NPRETAILERNAME());
            itemAsistencia.setAV_NPSTORENAME(listaEntradasItems[i].getAV_NPSTORENAME());

            listaFinal.add(itemAsistencia);
        }

        for (int i = 0; i < listaSalidasItems.length; i++) {
            BeanConsultAttendanceSearch itemAsistencia = 
                new BeanConsultAttendanceSearch();

            itemAsistencia.setAD_NPDATE(listaSalidasItems[i].getAD_EXITDATE());
            itemAsistencia.setAV_NPMOVTYPE("Salida");
            itemAsistencia.setAV_NPPOSNAME(listaSalidasItems[i].getAV_NPPOSNAME());
            itemAsistencia.setAV_NPPROMOTERNAME(listaSalidasItems[i].getAV_NPPROMOTERNAME());
            itemAsistencia.setAV_NPRETAILERNAME(listaSalidasItems[i].getAV_NPRETAILERNAME());
            itemAsistencia.setAV_NPSTORENAME(listaSalidasItems[i].getAV_NPSTORENAME());

            listaFinal.add(itemAsistencia);
        }
        return listaFinal;
    }*/

    //CRM 17-08-09 CONSULTA DE PROMOTORES
    public ActionForward promoterHome(ActionMapping mapping, ActionForm form, 
                                      HttpServletRequest request, 
                                      HttpServletResponse response) throws Exception {

            ConsultForm consultForm = (ConsultForm)form;
            logger.info("Inicio promoterHome");
            consultForm.setCmbRetailer("");
            consultForm.setCmbStore("");
            consultForm.setCmbPOS("");
            consultForm.setCmbPromoter("");
            consultForm.setCmbNextelSupervisor("");
            consultForm.setCmbFieldSupervisor("");
            consultForm.setTxtNumberDocument("");
            consultForm.setTxtNumberPhone("");
            request.getSession().removeAttribute("listaPromotores");

            INpParameterService parameterService = 
                (INpParameterService)getInstance("NpParameterService");
            //CARGAMOS EL COMBO SUPERVISOR NEXTEL
            consultForm.setLstNextelSupervisor(parameterService.getEntityByxNameDomain(Constant.SUPERVISORNEXTEL));
            //CARGAMOS EL COMBO SUPERVISOR DE CAMPO
            consultForm.setLstFieldSupervisor(parameterService.getEntityByxNameDomain(Constant.SUPERVISORCAMPO));
            //Se carga el combo Cadena
            INpRetailerService retailService = 
                (INpRetailerService)getInstance("NpRetailerService");
            consultForm.setLstRetail(retailService.getEntities(null));
            request.getSession().setAttribute("ListaCadena", 
                                              consultForm.getLstRetail());
            //Combo Sucursales
            List<NpStore> listaStore = new ArrayList<NpStore>();
            request.getSession().setAttribute("listaSucursal", listaStore);

            //Combo POS
            List<NpPos> listaPos = new ArrayList<NpPos>();
            request.getSession().setAttribute("listaPos", listaPos);

            //Combos Promotor
            List<NpPromoter> listaPromotor = new ArrayList<NpPromoter>();
            request.getSession().setAttribute("listaPromotor", listaPromotor);

            logger.info("Fin promoterHome");
            return mapping.findForward("promoterSearch");
    }

    //CRM 17-08-09 CONSULTA DE PROMOTORES, CUANDO SE PRESIONA EL BOTON BUSCAR
    public ActionForward promoterSearch(ActionMapping mapping, ActionForm form, 
                                        HttpServletRequest request, 
                                        HttpServletResponse response) throws Exception {

            ConsultForm consultForm = (ConsultForm)form;
            INpPromoterService iNpPromoterService = (INpPromoterService) getInstance("NpPromoterService"); //lvalencia
            List<NpPromoter> promoterLst = iNpPromoterService.getPromoters( consultForm.getCmbRetailer() != null && !consultForm.getCmbRetailer().equals("") ? new Integer(consultForm.getCmbRetailer()) : 0,
                                                                            consultForm.getCmbStore() != null && !consultForm.getCmbStore().equals("") ? new Integer(consultForm.getCmbStore()) : 0,
                                                                            consultForm.getCmbPOS() != null && !consultForm.getCmbPOS().equals("") ? new Integer(consultForm.getCmbPOS()) : 0,
                                                                            consultForm.getCmbPromoter() != null && !consultForm.getCmbPromoter().equals("") ? new Integer(consultForm.getCmbPromoter()) : 0,
                                                                            consultForm.getTxtNumberPhone(), consultForm.getTxtNumberDocument(),
                                                                            consultForm.getCmbFieldSupervisor() != null && !consultForm.getCmbFieldSupervisor().equals("") ? new Integer(consultForm.getCmbFieldSupervisor()) : 0,
                                                                            consultForm.getCmbNextelSupervisor() != null && !consultForm.getCmbNextelSupervisor().equals("") ? new Integer(consultForm.getCmbNextelSupervisor()) : 0, null); //lvalencia
            
             request.getSession().setAttribute("listaPromotores", promoterLst);
            
            //Se carga de nuevo el combo sucursal para que no se pierda cuando sumita la pagina.
            NpRetailer retail = new NpRetailer();
            if (request.getParameter("cmbRetailer") != null && 
                !request.getParameter("cmbRetailer").equals(""))
                retail.setNpretailerid(new Long(request.getParameter("cmbRetailer")));
            else
                retail.setNpretailerid(null);
            NpStore store = new NpStore();
            store.setNpRetailer(retail);
            //Traemos las sucursales que pertenecen a una Cadena
            INpStoreService storeService = 
                (INpStoreService)getInstance("NpStoreService");
            List<NpStore> listaStore = 
                storeService.getEntityByProperty(null, store);
            request.getSession().setAttribute("listaSucursal", listaStore);
            //Se carga de nuevo el combo Punto de Venta para que no se pierda cuando sumite
            if (request.getParameter("cmbStore") != null && 
                !request.getParameter("cmbStore").equals(""))
                store.setNpstoreid(new Long(request.getParameter("cmbStore")));
            else
                store.setNpstoreid(null);

            NpPos pos = new NpPos();
            pos.setNpStore(store);
            INpPosService posService = 
                (INpPosService)getInstance("NpPosService");
            List<NpPos> listaPos = posService.getEntityByProperty(null, pos);
            request.getSession().setAttribute("listaPos", listaPos);
            // Se carga de nuevo el combo promotor para que no se pierda cuando sumite
            if (request.getParameter("cmbPOS") != null && 
                !request.getParameter("cmbPOS").equals(""))
                pos.setNpposid(new Long(request.getParameter("cmbPOS")));
            else
                pos.setNpposid(null);

            NpPromoter promoter = new NpPromoter();
            promoter.setNpPos(pos);
            INpPromoterService promotorService = 
                (INpPromoterService)getInstance("NpPromoterService");
            //stockForm.setLstPromoter(promotorService.getEntityByProperty(null,promoter));
            List<NpPromoter> listaPromotor = 
                promotorService.getPromoterxIdPos(null, promoter);
            request.getSession().setAttribute("listaPromotor", listaPromotor);
            return mapping.findForward("promoterSearch");
    }

   /* public ArrayList cargarBeanConsultPromoter(RETAILNP_TYPES_PROMOTER_PKG_TR_CONS[] listaItems) {
        try {
            logger.info("Inicio cargarBeanConsultPromoter");
            int k = 0;
            ArrayList listaObtenidos = new ArrayList();
            while (listaItems != null && listaItems.length > k) {
                BeanConsultPromoter bean = 
                    obtenerConsultPromoter(listaItems[k]);
                listaObtenidos.add(bean);
                k++;
            }
            logger.info("convertToArray");
            return listaObtenidos;
        } catch (Exception e) {
            logger.error("", e);
        }
        return null;
        }*/

   /* public BeanConsultPromoter obtenerConsultPromoter(RETAILNP_TYPES_PROMOTER_PKG_TR_CONS item) {
        try {
            logger.info("Inicio obtenerConsultPromoter");
            BeanConsultPromoter bean = new BeanConsultPromoter();
            bean.setAV_RETAILNAME(item.getAV_RETAILNAME());
            bean.setAV_STORENAME(item.getAV_STORENAME());
            bean.setAV_POSNAME(item.getAV_POSNAME());
            bean.setAV_NPPROMOTERCODE(item.getAV_NPPROMOTERCODE());
            bean.setAD_DATESTART(item.getAD_DATESTART());
            bean.setAD_DATEEND(item.getAD_DATEEND());
            bean.setAV_NPPROMOTERNAME(item.getAV_NPPROMOTERNAME());
            bean.setAV_DOCUMENTTYPE(item.getAV_DOCUMENTTYPE());
            bean.setAV_NPDOCUMENTNUMBER(item.getAV_NPDOCUMENTNUMBER());
            bean.setAV_NPPHONENUMBER(item.getAV_NPPHONENUMBER());
            bean.setAV_NPPROMOTERSTATUS(item.getAV_NPPROMOTERSTATUS());
            bean.setAV_SUPERVISORCAMPO(item.getAV_SUPERVISORCAMPO());
            bean.setAV_NEXTELSUPERVISOR(item.getAV_NEXTELSUPERVISOR());
            return bean;
        } catch (Exception e) {
            logger.error("", e);
        }
        return null;
        }*/

    /*---------------------------------------------------------------------------------------------------------------------
    -- Purpose: Retorna el Forward "SearchCoveragePOS" que redicciona a la página que listará el Stock Disponible y 
    -- Consumo por Productos. Se encraga de realizar la búsuqeda y mostrar los resultados en la grilla.
    -- MODIFICATION HISTORY
    -- Person     Date         Comments
    -- ---------  ----------   ------------------------------------------------------------------------------------------
    -- YRUIZ      08/10/2013   SAR N_O000006450, Gerenación del Nuevo Reporte "Equipos > Cobertura por Punto de Venta"
    */    
    public ActionForward coveragePointOfSale(ActionMapping mapping, 
                                             ActionForm form, 
                                             HttpServletRequest request, 
                                             HttpServletResponse response) throws Exception {

            logger.info("Inicio coveragePointOfSale");
            List<BeanCoveragePointOfSale> listacoverageProductPOS = null;
            BeanCoveragePointOfSale bean = null;
            
            ConsultForm consultForm = (ConsultForm)form;           
            request.getSession().removeAttribute("listacoverageProductPOS");            
            
            INpProductService productService = (INpProductService)getInstance("NpProductService");
                            
            //Muestra los combos si es administrador            
            String rol = (String)request.getSession().getAttribute("rol");
            if (rol != null && rol.equals("admin")) {
                //Se carga el combo Cadena
                consultForm.setMostrarCombos(new Long(0)); // Si es cero se muestra los combo    
                
                if (consultForm.getLstGiro() == null){
                    //Se inicializa la lista de tipos de cadenas (tambien llamado grio)
                    INpParameterService parameterService = (INpParameterService)getInstance("NpParameterService");  
                    consultForm.setLstGiro(parameterService.getEntityByxNameDomain(Constant.BUSINESSRETAIL));                
                    request.getSession().setAttribute("ListaGiro", consultForm.getLstGiro());
                }
                
                if (consultForm.getLstRetail() == null){
                    //Se inicializa la lista de cadenas                                   
                    INpRetailerService retailService = (INpRetailerService)getInstance("NpRetailerService");
                    consultForm.setLstRetail(retailService.getEntities(null));
                    request.getSession().setAttribute("ListaCadena", consultForm.getLstRetail());                  
                }
                
                if (consultForm.getLstNextelSupervisor() == null){
                    //Se inicializa la lista de sucursales
                    List<NpStore> lstStore = new ArrayList<NpStore>();
                    
                    //PARA QUE CARGE LOS COMBOS SUCURSAL 
                    NpRetailer retail = new NpRetailer();
                    if (request.getParameter("cmbRetailer") != null && !request.getParameter("cmbRetailer").equals(""))
                        retail.setNpretailerid(new Long(request.getParameter("cmbRetailer")));
                    else
                        retail.setNpretailerid(null);
                    
                    NpStore store = new NpStore();
                    store.setNpRetailer(retail);
                    //Traemos las sucursales que pertenecen a una Cadena     
                    INpStoreService storeService = (INpStoreService)getInstance("NpStoreService");
                    List<NpStore> listaStore = storeService.getEntityByProperty(null, store);
                    if(logger.isDebugEnabled()){
                       logger.debug(listaStore.size());
                    }
                    consultForm.setLstStore(listaStore);
                    request.getSession().setAttribute("listaSucursal", consultForm.getLstStore());  
                }                
            } else {

                consultForm.setMostrarCombos(new Long(1)); // Si es uno se oculta los combos                                                  
                consultForm.setTxtGiro((String)request.getSession().getAttribute("NameGiro"));               
                consultForm.setTxtRetailer((String)request.getSession().getAttribute("NameRetail"));
                consultForm.setTxtStore((String)request.getSession().getAttribute("NameStore"));
                consultForm.setIdStore((Long)request.getSession().getAttribute("IdStore"));
                consultForm.setIdRetail((Long)request.getSession().getAttribute("IdRetail"));
                consultForm.setIdGiro((Long)request.getSession().getAttribute("IdGiro"));                
            }
            
            bean = new BeanCoveragePointOfSale();
            bean.setNpgiroid(consultForm.getCmbGiro() != 
                             null && 
                             !consultForm.getCmbGiro().equals("") ? 
                             new Long(consultForm.getCmbGiro()) : 
                             0);
            
            bean.setNpretailerid(consultForm.getCmbRetailer() != 
                             null && 
                             !consultForm.getCmbRetailer().equals("") ? 
                             new Long(consultForm.getCmbRetailer()) : 
                             0);
            
            bean.setNpstoreid(consultForm.getCmbStore() != 
                             null && 
                             !consultForm.getCmbStore().equals("") ? 
                             new Long(consultForm.getCmbStore()) : 
                             0);
            
            listacoverageProductPOS = productService.getCoverageProductByPOS(bean);
            request.getSession().setAttribute("listacoverageProductPOS", listacoverageProductPOS);            
            
            logger.info("Fin coveragePointOfSale");            
            return mapping.findForward("SearchCoveragePOS");
    }
    
    /*---------------------------------------------------------------------------------------------------------------------
    -- Purpose: Retorna el Forward "SearchCoveragePOS" Se encarga de llenar los combos sin mostrar la grilla con datos.
    -- MODIFICATION HISTORY
    -- Person     Date         Comments
    -- ---------  ----------   ------------------------------------------------------------------------------------------
    -- YRUIZ      08/10/2013   SAR N_O000006450, Gerenación del Nuevo Reporte "Equipos > Cobertura por Punto de Venta"
    */    
    public ActionForward defaultSearchCoveragePOS(ActionMapping mapping, 
                                             ActionForm form, 
                                             HttpServletRequest request, 
                                             HttpServletResponse response) throws Exception {

            logger.info("Inicio defaultSearchCoveragePOS");
            List<BeanCoveragePointOfSale> listacoverageProductPOS = null;
            BeanCoveragePointOfSale bean = null;
            
            ConsultForm consultForm = (ConsultForm)form;           
            request.getSession().removeAttribute("listacoverageProductPOS");
            
            //Muestra los combos si es administrador            
            String rol = (String)request.getSession().getAttribute("rol");
            if (rol != null && rol.equals("admin")) {
                //Se carga el combo Cadena
                consultForm.setMostrarCombos(new Long(0)); // Si es cero se muestra los combo    
                
                if (consultForm.getLstGiro() == null){
                    //Se inicializa la lista de tipos de cadenas (tambien llamado grio)
                    INpParameterService parameterService = (INpParameterService)getInstance("NpParameterService");  
                    consultForm.setLstGiro(parameterService.getEntityByxNameDomain(Constant.BUSINESSRETAIL));                
                    request.getSession().setAttribute("ListaGiro", consultForm.getLstGiro());
                }
                
                if (consultForm.getLstRetail() == null){
                    //Se inicializa la lista de cadenas                                   
                    INpRetailerService retailService = (INpRetailerService)getInstance("NpRetailerService");
                    consultForm.setLstRetail(retailService.getEntities(null));
                    request.getSession().setAttribute("ListaCadena", consultForm.getLstRetail());                  
                }
                
                if (consultForm.getLstStore() == null){
                    //Se inicializa la lista de sucursales
                    List<NpStore> lstStore = new ArrayList<NpStore>();
                    
                    //PARA QUE CARGE LOS COMBOS SUCURSAL 
                    NpRetailer retail = new NpRetailer();
                    if (request.getParameter("cmbRetailer") != null && !request.getParameter("cmbRetailer").equals(""))
                        retail.setNpretailerid(new Long(request.getParameter("cmbRetailer")));
                    else
                        retail.setNpretailerid(null);
                    
                    NpStore store = new NpStore();
                    store.setNpRetailer(retail);
                    //Traemos las sucursales que pertenecen a una Cadena     
                    INpStoreService storeService = (INpStoreService)getInstance("NpStoreService");
                    List<NpStore> listaStore = storeService.getEntityByProperty(null, store); 
                    if(logger.isDebugEnabled()){
                       logger.debug(listaStore.size());
                    }
                    consultForm.setLstStore(listaStore);
                    request.getSession().setAttribute("listaSucursal", consultForm.getLstStore());  
                }                
if (consultForm.getLstModality() == null){
                    //Se inicializa la lista de tipos de almacen (venta, consignacion, mixta)
                    INpParameterService parameterService = (INpParameterService)getInstance("NpParameterService");  
                    consultForm.setLstModality(parameterService.getEntityByxNameDomain(Constant.MODALIDAD));                
                    request.getSession().setAttribute("ListaModality", consultForm.getLstModality());
                }            
            } else {

                consultForm.setMostrarCombos(new Long(1)); // Si es uno se oculta los combos                                                  
                consultForm.setTxtGiro((String)request.getSession().getAttribute("NameGiro"));               
                consultForm.setTxtRetailer((String)request.getSession().getAttribute("NameRetail"));
                consultForm.setTxtStore((String)request.getSession().getAttribute("NameStore"));
                consultForm.setIdStore((Long)request.getSession().getAttribute("IdStore"));
                consultForm.setIdRetail((Long)request.getSession().getAttribute("IdRetail"));
                consultForm.setIdGiro((Long)request.getSession().getAttribute("IdGiro"));                
            }
            
            listacoverageProductPOS = new ArrayList<BeanCoveragePointOfSale>();
            request.getSession().setAttribute("listacoverageProductPOS", listacoverageProductPOS);
            
            logger.info("Fin defaultSearchCoveragePOS");            
            return mapping.findForward("SearchCoveragePOS");
    }
    
    public ActionForward exportToExcelCoveragePOS(ActionMapping mapping, 
                                         ActionForm form, 
                                         HttpServletRequest request, 
                                         HttpServletResponse response) throws Exception {

            logger.info("Inicio exportToExcelCoveragePOS");
            List<BeanCoveragePointOfSale> listacoverageProductPOS = null;
            BeanCoveragePointOfSale bean = null;
            
            ConsultForm consultForm = (ConsultForm)form;           
            request.getSession().removeAttribute("listacoverageProductPOS");            
            
            INpProductService productService = (INpProductService)getInstance("NpProductService");
            
            bean = new BeanCoveragePointOfSale();
            bean.setNpgiroid(consultForm.getCmbGiro() != 
                             null && 
                             !consultForm.getCmbGiro().equals("") ? 
                             new Long(consultForm.getCmbGiro()) : 
                             0);
            
            bean.setNpretailerid(consultForm.getCmbRetailer() != 
                             null && 
                             !consultForm.getCmbRetailer().equals("") ? 
                             new Long(consultForm.getCmbRetailer()) : 
                             0);
            
            bean.setNpstoreid(consultForm.getCmbStore() != 
                             null && 
                             !consultForm.getCmbStore().equals("") ? 
                             new Long(consultForm.getCmbStore()) : 
                             0);
            
bean.setNpmodality(consultForm.getCmbModality() != 
                         null && 
                         !consultForm.getCmbModality().equals("") ? 
                         new Long(consultForm.getCmbModality()) : 
                         0);   
                         
            bean.setNpdias(consultForm.getTxtDias() != 
                     null && 
                     !consultForm.getTxtDias().equals("") ? 
                     new Long(consultForm.getTxtDias()) : 
                     30); 

            listacoverageProductPOS = productService.getCoverageProductByPOS(bean);
            request.getSession().setAttribute("listacoverageProductPOS", listacoverageProductPOS);            
            logger.info("Fin exportToExcelCoveragePOS");
            
            return mapping.findForward("ReportCoveragePOS");
    }
    
    /*---------------------------------------------------------------------------------------------------------------------
    -- Purpose: Retorna el Forward "defaultSearchPromotersPOS" Se encarga de mostrar la pantalla de busqueda "Promotores por Punto de Venta"
    -- MODIFICATION HISTORY
    -- Person     Date         Comments
    -- ---------  ----------   ------------------------------------------------------------------------------------------
    -- YRUIZ      17/03/2014   N_O000007053, Gerenación del Nuevo Reporte "Promotores > Promotores por Punto de Venta"
    */
    public ActionForward defaultSearchPromotersPOS(ActionMapping mapping, 
                                             ActionForm form, 
                                             HttpServletRequest request, 
                                             HttpServletResponse response) throws Exception {

            logger.info("Inicio defaultSearchPromotersPOS");
            List<BeanPromoterPOS> listaBeanPromotersPOS = null;
            
            ConsultForm consultForm = (ConsultForm)form;           
            request.getSession().removeAttribute("listaPromotersPOS");
            
            //Muestra los combos si es administrador
            String rol = (String)request.getSession().getAttribute("rol");
            if (rol != null && rol.equals("admin")) {
                
                consultForm.setMostrarCombos(new Long(0)); // Si es cero se muestra los combo              
                consultForm.setCmbRetailer("");
                consultForm.setCmbNextelSupervisor("");
                consultForm.setTxtNextelSupervisor("");
                consultForm.setTxtRetailer("");
                consultForm.setTxtPromoterDateStartFrom("");
                consultForm.setTxtPromoterDateStartTo("");
                consultForm.setTxtPromoterDateEndFrom("");
                consultForm.setTxtPromoterDateEndTo("");
                
                //Se carga el combo Cadena
                if (consultForm.getLstRetail() == null){
                    //Se inicializa la lista de cadenas                                   
                    INpRetailerService retailService = (INpRetailerService)getInstance("NpRetailerService");
                    consultForm.setLstRetail(retailService.getEntities(null));
                    request.getSession().setAttribute("ListaCadena", consultForm.getLstRetail());                  
                }
                
                if (consultForm.getLstNextelSupervisor() == null){
                    //Se inicializa la lista de sucursales
                    INpParameterService parameterService = (INpParameterService)getInstance("NpParameterService");
                    //CARGAMOS EL COMBO SUPERVISOR NEXTEL
                    consultForm.setLstNextelSupervisor(parameterService.getEntityByxNameDomain(Constant.SUPERVISORNEXTEL));
                    request.getSession().setAttribute("lstNextelSupervisor", consultForm.getLstNextelSupervisor());
                }
            } else {
                consultForm.setMostrarCombos(new Long(1)); // Si es uno se oculta los combos                                                  
                consultForm.setTxtNextelSupervisor((String)request.getSession().getAttribute("NameSupervisor"));               
                consultForm.setTxtRetailer((String)request.getSession().getAttribute("NameRetail"));
                consultForm.setIdNextelSupervisor((Long)request.getSession().getAttribute("IdSupervisor"));
                consultForm.setIdRetail((Long)request.getSession().getAttribute("IdRetail"));                
            }
            
            listaBeanPromotersPOS = new ArrayList<BeanPromoterPOS>();
            BeanPromoterPOS bean = new BeanPromoterPOS();
            bean.setNpGiro(Constant.ITEM_NONE);
            bean.setNpCadena(Constant.ITEM_NONE);
            bean.setNpSucursal(Constant.ITEM_NONE);
            bean.setNpZona(Constant.ITEM_NONE);
            bean.setNpSupervisorNextel(Constant.ITEM_NONE);
            bean.setNpSupervisorCampo(Constant.ITEM_NONE);
            bean.setNpPOSName(Constant.ITEM_NONE);
            bean.setNpPromotorNonbre(Constant.ITEM_NONE);
            bean.setDocumento(Constant.ITEM_NONE);
            bean.setTelefono(Constant.ITEM_NONE);
            bean.setFecha_Inicio(Constant.DATE_NONE);
            bean.setFecha_Inicio(Constant.DATE_NONE);           
            bean.setEstado(Constant.ITEM_NONE);
            listaBeanPromotersPOS.add(bean);
            
            request.getSession().setAttribute("listaPromotersPOS", listaBeanPromotersPOS);
            
            logger.info("Fin defaultSearchPromotersPOS");            
            return mapping.findForward("SearchPromotersPOS");
    }  
    
    /*---------------------------------------------------------------------------------------------------------------------
    -- Purpose: Retorna el "searchPromotersPOS", Se encarga de mostrar el listado "Promotores por Punto de Venta" en formato HTML
    -- MODIFICATION HISTORY
    -- Person     Date         Comments
    -- ---------  ----------   ------------------------------------------------------------------------------------------
    -- YRUIZ      17/03/2014   N_O000007053, Gerenación del Nuevo Reporte "Promotores > Promotores por Punto de Venta"
    */
    public ActionForward searchPromotersPOS(ActionMapping mapping, 
                                         ActionForm form, 
                                         HttpServletRequest request, 
                                         HttpServletResponse response) throws Exception {

            logger.info("Inicio searchPromotersPOS");
            this.genericSearchPromotersPOS(mapping,form,request,response);
            logger.info("Fin searchPromotersPOS");            
            return mapping.findForward("SearchPromotersPOS");
    }
    
    public void genericSearchPromotersPOS(ActionMapping mapping, 
                                         ActionForm form, 
                                         HttpServletRequest request, 
                                         HttpServletResponse response) throws Exception {
            List<BeanPromoterPOS> listaBeanPromoterPOS = null;
            BeanPromoterPOS bean = null;
            
            ConsultForm consultForm = (ConsultForm)form;           
            request.getSession().removeAttribute("listaPromotersPOS");            
            
            INpPromoterService promoterService = (INpPromoterService)getInstance("NpPromoterService");
            
            bean = new BeanPromoterPOS();
            bean.setNpretailerid(consultForm.getCmbRetailer() != null && !consultForm.getCmbRetailer().equals("") ? 
                             new Integer(Integer.parseInt(consultForm.getCmbRetailer())) : 0);            
            bean.setNpsupervisorid(consultForm.getCmbNextelSupervisor()!= null && !consultForm.getCmbNextelSupervisor().equals("") ? 
                             new Integer(Integer.parseInt(consultForm.getCmbNextelSupervisor())) : 0);
            bean.setFechaIni_desde(consultForm.getTxtPromoterDateStartFrom() != null && !consultForm.getTxtPromoterDateStartFrom().equals("") ? 
                             new String(consultForm.getTxtPromoterDateStartFrom()) : null);
            bean.setFechaIni_hasta(consultForm.getTxtPromoterDateStartTo() != null && !consultForm.getTxtPromoterDateStartTo().equals("") ? 
                             new String(consultForm.getTxtPromoterDateStartTo()) : null);             
            bean.setFechaFin_desde(consultForm.getTxtPromoterDateEndFrom() != null && !consultForm.getTxtPromoterDateEndFrom().equals("") ? 
                             new String(consultForm.getTxtPromoterDateEndFrom()) : null); 
            bean.setFechaFin_hasta(consultForm.getTxtPromoterDateEndTo() != null && !consultForm.getTxtPromoterDateEndTo().equals("") ? 
                             new String(consultForm.getTxtPromoterDateEndTo()) : null); 
            listaBeanPromoterPOS = promoterService.getPromotersByPOS(bean);
            request.getSession().setAttribute("listaPromotersPOS", listaBeanPromoterPOS);            
    }
    
    /*---------------------------------------------------------------------------------------------------------------------
    -- Purpose: Retorna el "searchPromotersPOS", Se encarga de mostrar el listado "Promotores por Punto de Venta" en formato Excel
    -- MODIFICATION HISTORY
    -- Person     Date         Comments
    -- ---------  ----------   ------------------------------------------------------------------------------------------
    -- YRUIZ      17/03/2014   N_O000007053, Gerenación del Nuevo Reporte "Promotores > Promotores por Punto de Venta"
    */
    public ActionForward exportToExcelSearchPromotersPOS(ActionMapping mapping, 
                                         ActionForm form, 
                                         HttpServletRequest request, 
                                         HttpServletResponse response) throws Exception {

            logger.info("Inicio exportToExcelSearchPromotersPOS");
            this.genericSearchPromotersPOS(mapping,form,request,response);
            logger.info("Fin exportToExcelSearchPromotersPOS");            
            return mapping.findForward("ReportPromotersPOS");
    }
    
    //RPASCACIO-->metodo para insertar la cabecera de la Portabilidad en Consulta Previa  
        public ActionForward cabeceraPortabilityRegister(ActionMapping mapping, ActionForm form, 
                                         HttpServletRequest request, 
                                         HttpServletResponse response)throws Exception {

             int IdPortabilidad;       
             ConsultForm consultForm = (ConsultForm) form;
             INpPortabilidadServices portabilidadService = (INpPortabilidadServices)getInstance("NpPortabilidadServices");
             NpPortabilityCabecera portabilidadCabecera = new NpPortabilityCabecera();
             String[] documenTypeTemp = consultForm.getCmbDocumentType().split("\\|");
             String[] cedentTypeTemp = consultForm.getCmbCedenteType().split("\\|");
             //Cabecera
             portabilidadCabecera.setNpdocumenttype(Integer.valueOf(StringUtils.notNull(documenTypeTemp[0], Constant.TYPE_INTEGER)));       
             portabilidadCabecera.setNpdocumentnumber(consultForm.getTxtDocumentNumber());          
             portabilidadCabecera.setNpcedentetype(Integer.valueOf(StringUtils.notNull(cedentTypeTemp[0], Constant.TYPE_INTEGER)));                 
             portabilidadCabecera.setNpfirstlastname(consultForm.getTxtCustomerName());         
             portabilidadCabecera.setNpemail(consultForm.getTxtCustomerEmail());         
             portabilidadCabecera.setNpphonenumber(consultForm.getTxtCustomerTelefono());
             //portabilidadCabecera.setNpphonenumber(consultForm.getTxtCustomerTelefono());
             portabilidadCabecera.setNpposid(Integer.parseInt(request.getSession().getAttribute("IdPos").toString()));
             portabilidadCabecera.setNppromoterid(Integer.parseInt(request.getSession().getAttribute("IdPromoter").toString()));
             
             //CAMBIO FIJO MOVIL
             portabilidadCabecera.setNpservicetype(consultForm.getServiceType());
            //Se inserta la cabecera y detalle de la consulta                       
            //Detalle
             List<NpPortabilityDetalle> lstDetalle = new ArrayList<NpPortabilityDetalle>();                                                                                                                    
                    if(consultForm.getTxtPhoneNumberPortability()!=null) {   
                              for(int i=0;i<consultForm.getTxtPhoneNumberPortability().length;i++){
                                    NpPortabilityDetalle detalle = new NpPortabilityDetalle();                                                            
                                    detalle.setEstadoConsultaPrevia("");
                                    detalle.setMotivoRechazo("");
                                    detalle.setNportabilityDetailId(0);
                                    //detalle.setNpphonenumber(processMsgItemBean.getStrPhoneNumber());
                                    detalle.setModalidad(0);
                                    detalle.setMontoAdeudado(0);
                                    detalle.setNpphonenumber(StringUtils.notNull(consultForm.getTxtPhoneNumberPortability()[i], Constant.TYPE_STRING));
                                    detalle.setTipoMoneda("");
                                    detalle.setFechaVencimiento("");
                                    if(consultForm.getCmbOrigen()[i] != null){
                                              detalle.setModalidadStr(consultForm.getCmbOrigen()[i]);
                                            String[] arrModality = detalle.getModalidadStr().split("\\|");
                                              detalle.setModalidad(Integer.parseInt(arrModality[0]));
                                    }
                                    if(consultForm.getTxtRentaMensual()[i] != null){
                                              detalle.setRentaMensual(StringUtils.notNull(consultForm.getTxtRentaMensual()[i], Constant.TYPE_STRING));
                                           }
                                    lstDetalle.add(detalle);
                                }
                                
                                    int nReg = consultForm.getTxtPhoneNumberPortability().length;
                                    String[] aTxts = new String[nReg];
                                    
                                    for(int i=0;i<nReg;i++){
                                             aTxts[i]="";
                                    }                                
                                    consultForm.setTxtEstadoConsultaPrevia(aTxts);
                                    consultForm.setTxtMotivoRechazo(aTxts);
                                    consultForm.setTxtMontoAdeudado(aTxts);
                                    consultForm.setTxtFecVencimiento(aTxts);
                                    consultForm.setTxtTipoMoneda(aTxts);                                  
                                    consultForm.setTxtFechaActivacion(aTxts);
                                                      
                       
                                                      
                                portabilidadCabecera.setNpdetalle(lstDetalle);
                                IdPortabilidad = portabilidadService.insertPortabilityCabecera(portabilidadCabecera);
                                consultForm.setIntNpPortabilityCabeceraId(IdPortabilidad);
                                
                        //INI RPASCACIO - 25-07-2014: Inicio llmar al OSB CrmIntegration_SB/GetProcessMsgPortability
                        if (IdPortabilidad > 0 ){
                            HashMap hshResultPorta = portabilidadService.geProcessReceptor(IdPortabilidad);
                            
                            if (hshResultPorta.get("isError").equals("0")){
                                request.getSession().setAttribute("MostrarPortabilidad","Se realizo la consulta.");
                                request.getSession().setAttribute("strErrorPortability","0");
                             // return mapping.findForward("ConsultPreview");
                            }else if(hshResultPorta.get("isError").equals("1")){
                                request.getSession().setAttribute("MostrarPortabilidad","Error al llamar al servicio de consulta de portabilidad.");
                                request.getSession().setAttribute("strErrorPortability","1");
                             //return mapping.findForward("ConsultPreview");
                            }
                        }
                        //INI RPASCACIO - 25-07-2014
                    }
              
           return mapping.findForward("ConsultPreviewEnd");
   }
        
    public ActionForward sessionDelete(ActionMapping mapping, ActionForm form, 
                                     HttpServletRequest request, 
                                     HttpServletResponse response) throws Exception{     

         ConsultForm consultForm = (ConsultForm) form;
        
         consultForm.setTxtDocumentNumber("");
         consultForm.setTxtCustomerName("");
         consultForm.setTxtCustomerEmail("");
         consultForm.setTxtCustomerTelefono("");
         consultForm.setTxtPhoneNumberPortability(null);
         consultForm.setCmbOrigen(null);
         consultForm.setTxtRentaMensual(null);
         consultForm.setCmbCedenteType("");
         consultForm.setCmbDocumentType("");
     
       return mapping.findForward("ConsultPreview");
    }   
    
    
    public ActionForward obtenerRespuesta(ActionMapping mapping, ActionForm form, 
                                     HttpServletRequest request, 
                                     HttpServletResponse response)throws Exception {  
           ConsultForm consultForm = (ConsultForm) form;
           INpPortabilidadServices portabilidadService = (INpPortabilidadServices)getInstance("NpPortabilidadServices");           
               
              // NpPortabilidadDAO npPortabilidadDAO=new NpPortabilidadDAO();
               int npPortabilityid=consultForm.getIntNpPortabilityCabeceraId();
               if (npPortabilityid>0){
                  List<NpPortabilityDetalle> lista=portabilidadService.recuperarDatos(npPortabilityid);
                  /*  List<NpPortabilityDetalle> lista=new ArrayList<NpPortabilityDetalle>();
                   NpPortabilityDetalle ele1 = new NpPortabilityDetalle();
                   NpPortabilityDetalle ele2 = new NpPortabilityDetalle();
                   ele1.setEstadoConsultaPrevia("Rechazo de Consulta Previa por el ABDCP por Rechazo del ABDCP");
                   ele1.setMotivoRechazo("El numero telefonico no pertenece al Concesionario cedente indicado");

                   ele2.setEstadoConsultaPrevia("Rechazo de Consulta Previa por el ABDCP por Rechazo del ABDCP");
                   ele2.setMotivoRechazo("El numero telefonico no pertenece al Concesionario cedente indicado");
                   
                   lista.add(ele1);
                   lista.add(ele2);*/
                   if (lista!=null){
                       int nReg = lista.size();
                       String[] aTxts1 = new String[nReg];
                       String[] aTxts2 = new String[nReg];
                       String[] aTxts3 = new String[nReg];
                       String[] aTxts4 = new String[nReg];
                       String[] aTxts5 = new String[nReg];
                       String[] aTxts6 = new String[nReg];                       
                       
                   for(int i=0;i<lista.size();i++){
                       NpPortabilityDetalle npPortabilityDetalle = lista.get(i);
                       aTxts1[i] =StringUtils.notNull(npPortabilityDetalle.getEstadoConsultaPrevia(), Constant.TYPE_STRING).trim();
                       aTxts2[i] =StringUtils.notNull(npPortabilityDetalle.getMotivoRechazo(), Constant.TYPE_STRING);
                       aTxts3[i] =(npPortabilityDetalle.getMontoAdeudado()!=0?String.valueOf(npPortabilityDetalle.getMontoAdeudado()):"0");
                       aTxts4[i] =StringUtils.notNull(npPortabilityDetalle.getTipoMoneda(),Constant.TYPE_STRING);
                       aTxts5[i] =StringUtils.notNull(npPortabilityDetalle.getFechaVencimiento(),Constant.TYPE_STRING);
                       aTxts6[i] =StringUtils.notNull(npPortabilityDetalle.getFechaActivacion(),Constant.TYPE_STRING);//JROQUE
                   }  
                   
                        
                   consultForm.setTxtEstadoConsultaPrevia(aTxts1);
                   consultForm.setTxtMotivoRechazo(aTxts2);
                   consultForm.setTxtMontoAdeudado(aTxts3);
                   consultForm.setTxtFecVencimiento(aTxts4);
                   consultForm.setTxtTipoMoneda(aTxts5);           
                   consultForm.setTxtFechaActivacion(aTxts6);//JROQUE
                   }
                   request.setAttribute("MostrarDatosConsulta","listadoOk");                         
               }
    
      request.setAttribute("btnRespuestaEnable", false);

      return mapping.findForward("ConsultPreviewEnd");
   }

    public ActionForward ConsultPortaRetail(ActionMapping mapping, 
                                         ActionForm form, 
                                         HttpServletRequest request, 
                                         HttpServletResponse response) throws Exception {

            ConsultForm consultForm = (ConsultForm)form;  
            INpParameterService parameterService = (INpParameterService)getInstance("NpParameterService");
        
            consultForm.setCmbDocumentType("");
            consultForm.setTxtDocumentNumber("");
            consultForm.setTxtNumberPhone("");
            
            request.getSession().setAttribute("ListaPortabilidad",null);
            
            //Cargamos el combo tipo documento                    
            consultForm.setLstDocumentType(parameterService.getEntityByxNameDomain(Constant.TIPODOCUMENTOPORTABILIDAD));
            request.getSession().setAttribute("ListaTipoDocumento",consultForm.getLstDocumentType());
            return mapping.findForward("ConsultPortaRetail");
    }

    public ActionForward PortabilidadSearch(ActionMapping mapping, 
                                         ActionForm form, 
                                         HttpServletRequest request, 
                                         HttpServletResponse response) throws Exception {

            request.setAttribute("sErrorMsgRequest","");
            
            ConsultForm consultForm = (ConsultForm) form;
            INpPortabilidadServices portabilidadService = (INpPortabilidadServices)getInstance("NpPortabilidadServices");
           
            INpOrderService orderService =  (INpOrderService)getInstance("NpOrderService");
           
            NpPortabilidad bean = new NpPortabilidad();
            String[] documento =  consultForm.getCmbDocumentType().split("\\|");
            bean.setNpDocumentNumber(consultForm.getTxtDocumentNumber());
            bean.setNpDocumentType(Integer.parseInt(documento[0]));
            bean.setNpphonenumber(consultForm.getTxtNumberPhone());
            bean.setNpcreatedby(consultForm.getTxtDateHome());
            bean.setNpfecejecucion(consultForm.getTxtDateEnd());
         
            INpParameterService parameterService = 
                (INpParameterService)getInstance("NpParameterService");
         
         List<NpParameter> minutos = parameterService.getEntityByxNameDomain(Constant.MINUTOS_REPROCESO);
         
         List<NpParameter> minutosOrdenes = parameterService.getEntityByxNameDomain(Constant.MINUTOS_PORTABILIDAD);
    
         List<NpPortabilidad> portabilidad = portabilidadService.consultaPortabilidadRetail(bean);
          
        for( int i = 0 ; i<portabilidad.size() ; i++){
            Double diferent =  null;
            Double diferentInicial =  null;
            DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            SimpleDateFormat dt1 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String startDateRegistro = portabilidad.get(i).getFechaCreacion();
            Date frmDateRegistro = sdf.parse(StringUtils.notNull(startDateRegistro, Constant.TYPE_STRING));   
            diferentInicial=orderService.getBetweenDate(dt1.format(frmDateRegistro));
            
            if (portabilidad.get(i).getFechaReProceso()!=null){
                String startDate = portabilidad.get(i).getFechaReProceso();
                Date frmDate = sdf.parse(StringUtils.notNull(startDate, Constant.TYPE_STRING));
                if(logger.isDebugEnabled()){
                   logger.debug(dt1.format(frmDate)); 
                }
                diferent=orderService.getBetweenDate(dt1.format(frmDate));
            }
            
          
            //Verifica que el message estatus este registrado
            /*if(portabilidad.get(i).getNpstatusportability()==null || portabilidad.get(i).getNpstatusportability().equals("") )
               {
               if(diferentInicial > Double.parseDouble(minutosOrdenes.get(0).getNpparametervalue1())) {
                   portabilidad.get(i).setResp1(1);  
               }        
            }   
            else {*/
                
                portabilidad.get(i).setResp1(portabilidadService.ReprocesoPortabilidadRetail(portabilidad.get(i).getIdgenerico(),1)); 
                portabilidad.get(i).setResp2(1);  
               
                if(portabilidad.get(i).getResp1()==0){
                portabilidad.get(i).setResp1(portabilidadService.ReprocesoPortabilidadRetail(portabilidad.get(i).getIdgenerico(),2));       
                portabilidad.get(i).setResp2(2);
                    }
                    
                if(portabilidad.get(i).getResp1()==1){
                    if (diferent!=null ){
                        if(diferent < Double.parseDouble(minutos.get(0).getNpparametervalue1())){
                           portabilidad.get(i).setResp1(0);   
                        }
                    }
                }
                
            //}
        }   
         request.getSession().setAttribute("ListaPortabilidad",portabilidad);
           
        return mapping.findForward("ConsultPortaRetail");
    }

    public ActionForward ReprocesarOrder(ActionMapping mapping, 
                                         ActionForm form, 
                                         HttpServletRequest request, 
                                         HttpServletResponse response) throws Exception {
           
            WebServiceTemplate getProcessMsgPortabilityService = (WebServiceTemplate)ApplicationContext.getBean("getProcessMsgePortabilityService"); //vcedeno@soaint.com
            ConsultForm consultForm = (ConsultForm) form;
            INpPortabilidadServices portabilidadService = (INpPortabilidadServices)getInstance("NpPortabilidadServices"); 
            
            portabilidadService.ReprocesoUpdateFecha(consultForm.getTxtIdOrderItem(),consultForm.getTxtIdDeviceItem(),null);
           
            CreateMessage portability = ofGetProcessMsgPortability.createCreateMessage();
            CreateMessagePortabilityRequest requestPortability = ofGetProcessMsgPortability.createCreateMessagePortabilityRequest();
            requestPortability.setStrOrigin(Constant.ORIGIN);
            requestPortability.setStrPortabilityType(Constant.RETAIL_ALTA);
            requestPortability.setStrCustomerId(consultForm.getTxtcustomerid()+"");
            requestPortability.setStrLoginId(consultForm.getTxtpromotor());
            requestPortability.setStrOrderId(consultForm.getTxtnumeroid()+"");
          
          if (consultForm.getTxtrespt() == 1){
              requestPortability.setStrMessageType(Constant.RETAIL_SP);
          }
          else if (consultForm.getTxtrespt() == 2) {
              requestPortability.setStrMessageType(Constant.RETAIL_PP); 
          }
        
            portability.setArg0(requestPortability);
            JAXBElement<CreateMessage> messageRequest = ofGetProcessMsgPortability.createCreateMessage(portability);
         
            if(logger.isDebugEnabled()){
              logger.debug("Antes de invocarlo ");
            }
            JAXBElement<CreateMessagePortabilityResponse> createMessageResponse = (JAXBElement<CreateMessagePortabilityResponse>)getProcessMsgPortabilityService.marshalSendAndReceive(messageRequest);
            if(logger.isDebugEnabled()){
              logger.debug("Despues de invocarlo ");
            }
        
            request.getSession().setAttribute("ListaPortabilidad",null);
        
        return mapping.findForward("ConsultPortaRetail");
    }
    
    
    public ActionForward portaReport(ActionMapping mapping, ActionForm form, 
                HttpServletRequest request, HttpServletResponse response) throws Exception {

        logger.info("Inicio Reporte de Portabilidad");
        
        ConsultForm consultForm = (ConsultForm)form;
        
        consultForm.setCmbRetailer("");
        consultForm.setCmbStore("");
        consultForm.setCmbPOS("");
        consultForm.setCmbPromoter("");
        consultForm.setCmbOrderStatus("");
        consultForm.setCmbOrderType("");
        consultForm.setCmbSolution("");
        consultForm.setCmbProduct("");
        consultForm.setCmbPlan("");
        consultForm.setCmbLocalidad("");
        consultForm.setCmbNextelSupervisor("");
        consultForm.setCmbDepartment("");
        consultForm.setCmbProvince("");

        consultForm.setTxtDateEnd(DateUtils.fechaActualFormatoDDMMYYYY());
        consultForm.setTxtDateHome(DateUtils.fechaActualFormatoDDMMYYYY());
        
        INpParameterService parameterService = (INpParameterService)getInstance("NpParameterService");
        INpOrderService orderService = (INpOrderService)getInstance("NpOrderService");
        
        // COMBO ZONA GEOGRAFICA
        consultForm.setLstLocalidad(parameterService.getEntityByxNameDomain(Constant.TIPOZONAGEOGRAFICA));
        request.getSession().setAttribute("ListaLocalidad", consultForm.getLstLocalidad());

        // COMBO ESTADO DE LA ORDEN
        consultForm.setLstOrderStatus(parameterService.getEntityByxNameDomain(Constant.ORDER_STATUS_PORTA));
        request.getSession().setAttribute("ListaEstadoOrden", consultForm.getLstOrderStatus());

        // COMBO TIPO DE SOLUCION
        NpSolution s = new NpSolution();
        consultForm.setLstSolution(orderService.getSolution(s));
        request.getSession().setAttribute("ListaTipoSolucion", consultForm.getLstSolution());

        // COMBO CADENA
        INpRetailerService retailService = (INpRetailerService)getInstance("NpRetailerService");
        consultForm.setLstRetail(retailService.getEntities(null));
        request.getSession().setAttribute("ListaCadena", consultForm.getLstRetail());
        
        // COMBO PRODUCTOS GENERAL
        INpProductService productService = (INpProductService)getInstance("NpProductService");
        consultForm.setLstProductGeneral(productService.getEntities(null));
        request.getSession().setAttribute("ListaProductosGeneral", consultForm.getLstProductGeneral());
        
        // COMBO PLANES 
        INpPlanService planService = (INpPlanService)getInstance("NpPlanService");
        consultForm.setLstPlan(planService.lstPlanAll());
        request.getSession().setAttribute("listaPlan", consultForm.getLstPlan());
        
        // COMBO ZONA GEOGRAFICA
        consultForm.setLstZoneGeografic(parameterService.getEntityByxNameDomain(Constant.STATUSEVALUATION));
        request.getSession().setAttribute("ListaZonaGeografica", consultForm.getLstZoneGeografic());
        
        // COMBO SUCURSALES
        List<NpStore> listaStore = new ArrayList<NpStore>();
        request.getSession().setAttribute("listaSucursal", listaStore);

        // COMBO POS
        List<NpPos> listaPos = new ArrayList<NpPos>();
        request.getSession().setAttribute("listaPos", listaPos);

        // COMBO POROMOTOR
        List<NpPromoter> listaPromotor = new ArrayList<NpPromoter>();
        request.getSession().setAttribute("listaPromotor", listaPromotor);

        // COMBO ZONA GEOGRAFICA
        consultForm.setLstSaleZone(parameterService.getEntityByxNameDomain(Constant.RETAIL_ZONA_VENTA));
        
        // PARA OBTENER EL VALOR POR DEFECTO (RETAIL) DEL COMBO ZONA DE VENTA
        List<?> listDefSaleZone = parameterService.getParameterByDomainNameByParameterNameVal(Constant.RETAIL_ZONA_VENTA, Constant.RETAIL_LOWERCASE,2);
        NpParameter def = (listDefSaleZone != null? (listDefSaleZone.size() > 0?((NpParameter) listDefSaleZone.get(0)): null): null);
        Long salesZoneDefaultCode = null;
        if(def != null){
            salesZoneDefaultCode = def.getNpparameterid();
        }
        request.getSession().setAttribute("salesZoneDefaultCode", salesZoneDefaultCode);
        
        // COMBO SUPERVISOR NEXTEL
        consultForm.setLstNextelSupervisor(parameterService.getEntityByxNameDomain(Constant.SUPERVISORNEXTEL));
        request.getSession().setAttribute("ListaSupervisorNextel",consultForm.getLstNextelSupervisor());
        
        // COMBO DEPARTAMENTO
        INpUbigeoService ubigeoService = (INpUbigeoService)getInstance("NpUbigeoService");
        consultForm.setLstDepartment(ubigeoService.getEntities(null));
        request.getSession().setAttribute("ListaDepartamentos",consultForm.getLstDepartment());
        
        // COMBO Provincias
        List<NpUbigeo> listProvincia = new ArrayList<NpUbigeo>();
        request.getSession().setAttribute("listaProvincias",listProvincia);
        
        logger.info("Fin Reporte de Portabilidad");
      return mapping.findForward("consultPortability");
   }
    
    public ActionForward portabilityReport(ActionMapping mapping, ActionForm form,
                         HttpServletRequest request, HttpServletResponse response) throws Exception {

            ConsultForm consultForm = (ConsultForm) form;

            Map parametros = new HashMap();
            parametros.put("an_npretailerid", new Integer(StringUtils.notNull(consultForm.getCmbRetailer(), Constant.TYPE_INTEGER)));
            parametros.put("an_npstoreid", new Integer(StringUtils.notNull(consultForm.getCmbStore(), Constant.TYPE_INTEGER)));
            parametros.put("an_npposid", new Integer(StringUtils.notNull(consultForm.getCmbPOS(), Constant.TYPE_INTEGER)));
            parametros.put("an_nppromoterid", new Integer(StringUtils.notNull(consultForm.getCmbPromoter(), Constant.TYPE_INTEGER)));
            parametros.put("av_fromdate", new String(StringUtils.notNull(consultForm.getTxtDateHome(), Constant.TYPE_STRING)));
            parametros.put("av_todate", new String(StringUtils.notNull(consultForm.getTxtDateEnd(), Constant.TYPE_STRING)));
            parametros.put("an_nporderstatus", new Integer(StringUtils.notNull(consultForm.getCmbOrderStatus(), Constant.TYPE_INTEGER)));
            parametros.put("an_npsolutioncode", new Integer(StringUtils.notNull(consultForm.getCmbSolution(), Constant.TYPE_INTEGER)));
            parametros.put("an_productid", new Integer(StringUtils.notNull(consultForm.getCmbProduct(), Constant.TYPE_INTEGER)));
            parametros.put("an_locationid", new Integer(StringUtils.notNull(consultForm.getCmbLocalidad(), Constant.TYPE_INTEGER)));
            parametros.put("an_salezone", new Integer(StringUtils.notNull(consultForm.getCmbSaleZone(), Constant.TYPE_INTEGER)));
            parametros.put("an_npsupervisornextelid", consultForm.getCmbNextelSupervisor() != null && !consultForm.getCmbNextelSupervisor().equals("") ? new Integer(consultForm.getCmbNextelSupervisor()) : 0);
            parametros.put("av_npdepartmentid", consultForm.getCmbDepartment() != null && !consultForm.getCmbDepartment().equals("")? consultForm.getCmbDepartment() : null);
            parametros.put("av_npprovinceid", consultForm.getCmbProvince() != null && !consultForm.getCmbProvince().equals("")? consultForm.getCmbProvince() : null);                                                                                                                                                                     

            NpPortabilidadServices portabilidadService = (NpPortabilidadServices)getInstance("NpPortabilidadServices");
            List<NpLiquidation> liquidationList = portabilidadService.getPortabilityReport(parametros);
            parametros = null;
            portabilidadService = null;

            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment; filename=ReportePortabilidad.xls");

            PrintWriter out = response.getWriter();
            NotNullStringBuilderDecorator sb = new NotNullStringBuilderDecorator();
            
              sb.append("<html>");
              sb.append("<style>");
              sb.append(".style0{mso-number-format:General;");
              sb.append("text-align:general; vertical-align:bottom;");
              sb.append("white-space:nowrap;mso-rotate:0;");
              sb.append("mso-background-source:auto;mso-pattern:auto;");
              sb.append("color:windowtext;font-size:10.0pt;");
              sb.append("font-weight:400;font-style:normal;");
              sb.append("text-decoration:none;font-family:Arial;");
              sb.append("mso-generic-font-family:auto;mso-font-charset:0;");
              sb.append("border:none;mso-protection:locked visible;");
              sb.append("mso-style-name:Normal;mso-style-id:0;} ");
              sb.append(".xl67{mso-style-parent:style0; color:black;");
              sb.append("font-size:8.0pt;");
              sb.append("font-family:sansserif;mso-generic-font-family:auto;mso-font-charset:0;");
              sb.append("mso-number-format:\"dd\\/mm\\/yyyy\";");
              sb.append("text-align:left;vertical-align:top;white-space:normal;}");
              sb.append(".xl72{mso-style-parent:style0;");
              sb.append("color:black;font-size:8.0pt;font-family:sansserif;");
              sb.append("mso-generic-font-family:auto;mso-font-charset:0;");
              sb.append("mso-number-format:Standard;text-align:right;");
              sb.append("vertical-align:top;white-space:normal;}");
              sb.append(".xl68{mso-number-format:/”Long Time/”;}");
              sb.append(".xl71{mso-number-format:\\@;}");
              sb.append(".xl73{font-size:12.0pt;font-weight:bold;font-family:sansserif;}");
              sb.append(".x174{font-size:10.0pt;font-weight:bold;font-family:sansserif;}");
              sb.append("td{font-size:8.0pt;font-family:sansserif;}");
              sb.append("</style>");
              sb.append("<body><table>");
              sb.append("<tr><td class=xl67>");
              sb.append(DateUtils.fechaHoraActualFormatoDDMMYYYYHHSS());
              sb.append("</td>");
              sb.append("<td></td><td></td><td></td><td></td><td></td><td></td><td></td><td></td><td></td>");
              sb.append("<td></td><td></td><td></td><td></td><td></td><td></td><td collspan=4 class=xl73>Reporte de Portabilidad</td>");
              sb.append("<td></td><td></td><td></td><td></td><td></td><td></td><td></td><td></td><td></td><td></td>");
              sb.append("<td></td><td></td><td></td><td></td><td></td><td></td><td></td><td></td><td></td><td></td>");
              sb.append("<td></td><td></td><td></td><td></td><td></td><td></td><td></td></tr>");
              sb.append("<tr>");
              
              sb.append("<td class=x174>").append("Fecha").append("</td>");
              sb.append("<td class=x174>").append("Hora").append("</td>");
              sb.append("<td class=x174>").append("Tipo Cadena").append("</td>");
              sb.append("<td class=x174>").append("Departamento").append("</td>");
              sb.append("<td class=x174>").append("Provincia").append("</td>");
              sb.append("<td class=x174>").append("Localidad").append("</td>"); 
              sb.append("<td class=x174>").append("Cadena").append("</td>");
              sb.append("<td class=x174>").append("Sucursal").append("</td>");
              sb.append("<td class=x174>").append("Promotor").append("</td>");
              sb.append("<td class=x174>").append("DNI Promotor").append("</td>");
              sb.append("<td class=x174>").append("Datos Promotor").append("</td>");
              sb.append("<td class=x174>").append("Nro. Orden Venta").append("</td>");
              sb.append("<td class=x174>").append("Tipo").append("</td>");
              sb.append("<td class=x174>").append("División").append("</td>");
              sb.append("<td class=x174>").append("Solución").append("</td>");
              sb.append("<td class=x174>").append("Cod. Cliente").append("</td>");
              sb.append("<td class=x174>").append("Cliente").append("</td>");
              sb.append("<td class=x174>").append("Tipo Doc Cliente").append("</td>");
              sb.append("<td class=x174>").append("Nro. Doc Cliente(DNI/RUC)").append("</td>");
              sb.append("<td class=x174>").append("Producto").append("</td>");
              sb.append("<td class=x174>").append("Sku").append("</td>");
              sb.append("<td class=x174>").append("Modelo").append("</td>");
              sb.append("<td class=x174>").append("Plan Tarifario").append("</td>");
              sb.append("<td class=x174>").append("Precio Orden").append("</td>");
              sb.append("<td class=x174>").append("IMEI").append("</td>");
              sb.append("<td class=x174>").append("SIM").append("</td>");
              sb.append("<td class=x174>").append("Nro. Contrato").append("</td>");
              sb.append("<td class=x174>").append("Nro Teléfono").append("</td>");
              sb.append("<td class=x174>").append("Estado").append("</td>");
              sb.append("<td class=x174>").append("Teléfono a Portar").append("</td>");
              sb.append("<td class=x174>").append("Modalidad de Origen").append("</td>");
              sb.append("<td class=x174>").append("Estado Portabilidad").append("</td>");
              sb.append("<td class=x174>").append("Cedente").append("</td>");
              sb.append("<td class=x174>").append("Motivo de Rechazo Portabilidad").append("</td>");
              sb.append("<td class=x174>").append("Monto Adeudado Portabilidad").append("</td>");
              sb.append("<td class=x174>").append("Moneda").append("</td>");
              sb.append("<td class=x174>").append("Fec. Venc. Últ Fact.").append("</td>");
              sb.append("<td class=x174>").append("Fecha de Ejecución Portabilidad").append("</td>");
              sb.append("</tr>");

            if (liquidationList != null){
            
                for (NpLiquidation liquidBean: liquidationList) {
                    sb.append("<tr>");
                    sb.append("<td class=xl67>").append(liquidBean.getNporderdate()).append("</td>");
                    sb.append("<td class=xl68>").append(liquidBean.getNporderhour()).append("</td>");
                    sb.append("<td>").append(liquidBean.getNptyperetailer()).append("</td>");
                    sb.append("<td>").append(liquidBean.getNpdepartment()).append("</td>");
                    sb.append("<td>").append(liquidBean.getNpprovince()).append("</td>");
                    sb.append("<td>").append(liquidBean.getNplocalidad()).append("</td>");
                    sb.append("<td>").append(liquidBean.getNpretailer()).append("</td>");
                    sb.append("<td>").append(liquidBean.getNpstore()).append("</td>");
                    sb.append("<td>").append(liquidBean.getNppromotor()).append("</td>");
                    sb.append("<td>").append(liquidBean.getNpdnipromotor()).append("</td>");
                    sb.append("<td>").append(liquidBean.getNppromotername()).append("</td>");
                    sb.append("<td class=xl71>").append(liquidBean.getNpordernumber()).append("</td>");
                    sb.append("<td>").append(liquidBean.getNpordertypename()).append("</td>");
                    sb.append("<td>").append(liquidBean.getNpdivision()).append("</td>");
                    sb.append("<td>").append(liquidBean.getNpsolution()).append("</td>");
                    sb.append("<td>").append(liquidBean.getNpcodclient()).append("</td>");
                    sb.append("<td>").append(liquidBean.getNpcliente()).append("</td>");
                    sb.append("<td class=xl71>").append(liquidBean.getNpdocumenttype()).append("</td>");
                    sb.append("<td class=xl71>").append(liquidBean.getNpdocumentnumber()).append("</td>");
                    sb.append("<td>").append(liquidBean.getNpproducto()).append("</td>");
                    sb.append("<td class=xl71>").append(liquidBean.getNpsku()).append("</td>");
                    sb.append("<td>").append(liquidBean.getNpmodelo()).append("</td>");
                    sb.append("<td>").append(liquidBean.getNpplantarif()).append("</td>");
                    sb.append("<td class=xl72>").append(liquidBean.getNppriceequip()).append("</td>");
                    sb.append("<td class=xl71>").append(liquidBean.getNpimei()).append("</td>");
                    sb.append("<td class=xl71>").append(liquidBean.getNpsim()).append("</td>");
                    sb.append("<td class=xl71>").append(liquidBean.getNpnrocontrato()).append("</td>");
                    sb.append("<td class=xl71>").append(liquidBean.getNpnumberphone()).append("</td>");
                    sb.append("<td>").append(liquidBean.getNpstatusorder()).append("</td>");
                    NpPortabilidad portaBean = liquidBean.getPortabilidad();
                    sb.append("<td>").append(portaBean.getNpphonenumber()).append("</td>");
                    sb.append("<td>").append(portaBean.getNporigen()).append("</td>");
                    sb.append("<td>").append(portaBean.getNpstatusportability()).append("</td>");
                    sb.append("<td>").append(portaBean.getNpcedente()).append("</td>");
                    sb.append("<td>").append(portaBean.getNpmotivorechazo()).append("</td>");
                    sb.append("<td class=xl72>").append(portaBean.getNpmontoadeudado()).append("</td>");
                    sb.append("<td>").append(portaBean.getNpmoneda()).append("</td>");
                    sb.append("<td>").append(portaBean.getNpfecultfacturacion()).append("</td>");
                    sb.append("<td>").append(portaBean.getNpfecejecucion()).append("</td>");
                    sb.append("</tr>");
                }
                liquidationList = null;
            }else{
               sb.append("<tr>");
               sb.append("<td colspan=\"40\">").append("No Hay Resultados").append("</td>");
               sb.append("<td>");
           }           

            sb.append("</table></body></html>");

            out.print(sb.toString());
            sb = null;
            out.flush();
            out.close();
            out = null; 
            return null;
    }
    
    public ActionForward ConsultSubSanacionPorta(ActionMapping mapping, 
                                         ActionForm form, 
                                         HttpServletRequest request, 
                                         HttpServletResponse response) throws Exception {

            ConsultForm consultForm = (ConsultForm)form;  
            INpParameterService parameterService = (INpParameterService)getInstance("NpParameterService");
            consultForm.setCmbDocumentType("");
            consultForm.setTxtDocumentNumber("");
            
            request.getSession().setAttribute("ListaPortabilidadSubsanacion",null);
            
            //Cargamos el combo tipo documento                    
            consultForm.setLstDocumentType(parameterService.getEntityByxNameDomain(Constant.TIPODOCUMENTOPORTABILIDAD));
            request.getSession().setAttribute("ListaTipoDocumento",consultForm.getLstDocumentType());
            return mapping.findForward("ConsultSubSanacionPorta");
    }
    
    public ActionForward ConsultSubSanacionSearch(ActionMapping mapping, 
                                         ActionForm form, 
                                         HttpServletRequest request, 
                                         HttpServletResponse response) throws Exception {

            ConsultForm consultForm = (ConsultForm) form;
            INpPortabilidadServices portabilidadService = (INpPortabilidadServices)getInstance("NpPortabilidadServices");
           
            INpOrderService orderService =  (INpOrderService)getInstance("NpOrderService");
            
            NpPortabilidad bean = new NpPortabilidad();
            String[] documento =  consultForm.getCmbDocumentType().split("\\|");
            bean.setNpDocumentNumber(consultForm.getTxtDocumentNumber());
            bean.setNpDocumentType(Integer.parseInt(documento[0]));
            bean.setNpphonenumber(consultForm.getTxtNumberPhone());
            bean.setNpcreatedby(consultForm.getTxtDateHome());
            bean.setNpfecejecucion(consultForm.getTxtDateEnd());
         
            INpParameterService parameterService = (INpParameterService)getInstance("NpParameterService");
            List<NpParameter> minutos = parameterService.getEntityByxNameDomain(Constant.MINUTOS_REPROCESO);
            List<NpPortabilidad> portabilidad = portabilidadService.consultaSubSanacionPortaRetail(bean);
          
            for( int i = 0 ; i<portabilidad.size() ; i++){
                Double diferent =  null;
                
                if (portabilidad.get(i).getFechaReProceso()!=null){
                    DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    String startDate = portabilidad.get(i).getFechaReProceso();
                    Date frmDate = sdf.parse(StringUtils.notNull(startDate, Constant.TYPE_STRING));               
                    SimpleDateFormat dt1 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    if(logger.isDebugEnabled()){
                       logger.debug(dt1.format(frmDate));
                    }
                    
                    diferent=orderService.getBetweenDate(dt1.format(frmDate));
                }                                                
            }   
         
            request.getSession().setAttribute("ListaPortabilidadSubsanacion",portabilidad);           
            return mapping.findForward("ConsultSubSanacionPorta");
    }
    
    public ActionForward ProcessSubSanacion(ActionMapping mapping, 
                                         ActionForm form, 
                                         HttpServletRequest request, 
                                         HttpServletResponse response) throws Exception {

            WebServiceTemplate service = (WebServiceTemplate)ApplicationContext.getBean("processMessagePortabilityService"); //vcedenos@soaint.com
            OrderImagesHandler gestorImagenes = new OrderImagesHandler();
            
            Properties properties= new Properties();
            
            ConsultForm consultForm = (ConsultForm) form;
            INpPortabilidadServices portabilidadService = (INpPortabilidadServices)getInstance("NpPortabilidadServices");
            
            INpParameterService parameterService = (INpParameterService)getInstance("NpParameterService");
            
            ArrayList<NpDocument> documentListA = new ArrayList<NpDocument>();
            
            INpOrderService orderService =  (INpOrderService)getInstance("NpOrderService");
            
            //int orderRetail=consultForm.get;
            int orderPortal=0;
            
            byte[] compressNewImageInByte=null;
            
            
            try{

                    List<NpParameter> imageParametersList = gestorImagenes.getOrderImageTypes();
                    
                    String customerDocument = consultForm.getTxtDocumentNumber();
                    String imagePaths = gestorImagenes.obtainImagePaths(customerDocument+"_sub", imageParametersList);
                    String imageNames = gestorImagenes.obtainImageNames(customerDocument+"_sub", imageParametersList);
                    //String imageDocumentTypes = gestorImagenes.obtainImageDocumentTypes(customerDocument, imageParametersList);
         
                    //OBTENEMOS LA LISTA DE  ARCHIVOS DEL FORMULARIO                    
                    ArrayList myFiles =(ArrayList) consultForm.getFiles();
                    
                    String sDocTypesName="3";                    
                    int j=0;
                    for(int i=0;i<myFiles.size();i++){
                        if (myFiles.get(i) != null && (consultForm.getFileList(consultForm.getStrFileName())).contains(((FormFile)myFiles.get(i)).getFileName()) ) {
                            int indiceElement=(consultForm.getFileList(consultForm.getStrFileName())).indexOf(((FormFile)myFiles.get(i)).getFileName());
                            //ya que solo insertaremos el voucher nos ubicamos en la posicion del nombre y de la ruta esta posicion en el array de rutas es 2
                            j=2;
                            FormFile myFile =(FormFile)myFiles.get(i) ;
                            if(logger.isDebugEnabled()){
                               logger.debug("nombre File "+i+":"+myFile.getFileName());
                               logger.debug("nombre size "+i+":"+myFile.getFileSize());                                                
                            }
                            compressNewImageInByte=myFile.getFileData();    
                                                                                                   
                            
                            NpDocument npDocument=new NpDocument();
                            
                            int sz = 6;
                            Object[] conArray = new Object[sz];
                            byte[] fileArrayT = null;
                            //byte[] compressNewImageInByte=null;                                    
                            
                             if(logger.isDebugEnabled()){
                                logger.debug("FileSize File New imagen"+i+":"+compressNewImageInByte.length);
                             }
                            
                            fileArrayT = compressNewImageInByte;
                            
                            conArray[0] = fileArrayT;                            
                            conArray[1] = (String)(consultForm.getFileList(imagePaths).get(j));
                            conArray[2] = Integer.valueOf(Arrays.hashCode(fileArrayT));                     
                            conArray[3] = Integer.valueOf(myFiles.size());                                                    
                            conArray[4] = (String)((consultForm.getFileList(imageNames)).get(j));                            
                            conArray[5] = (String)(new String("3"));
                                            
                            byte[] fileArray =compressNewImageInByte;
                            npDocument.setNpimage(fileArray);
                            
                            String filePath =(String)conArray[1];//pathsString ruta completa, incluye nombre de archivo
                            npDocument.setNppathimage(filePath);
                            
                            int hashCodeReceived=(Integer)conArray[2];
                            npDocument.setNpimage_hashCodeReceived(hashCodeReceived);
                            
                            int numImages=(Integer)conArray[3];
                            
                            String names=(String)conArray[4];//names
                            npDocument.setNpnameimage(names);
                            
                            String types=(String)conArray[5];//types
                            npDocument.setNptypedocument(types);
                            
                            int hashCodeCalculated=java.util.Arrays.hashCode(fileArray);
                            npDocument.setNpimage_hashCodeCalculated(hashCodeCalculated);
                                                                                  
                            npDocument.setNpimage(fileArray);
                                            
                            documentListA.add(npDocument);  
                            j++;
                        }
                    }
            } catch(Exception e) {
               logger.error("", e);
            }            
            
            // MDP - 19/07/2016 - INICIO
            try{
            	validateFile(consultForm);
            }catch(Exception e){
            	logger.error(e.getMessage(), e);
            	request.setAttribute("sErrorMsgRequest", e.getMessage());
                return mapping.findForward("ConsultSubSanacionPorta");
            } 
//            if (checkFileSize(consultForm)==false){
//                //request.setAttribute("MsgFileSize","true");
//                request.setAttribute("sErrorMsgRequest","El tamaño de la imagen es superior al permitido (Ejm:120 KB)");
//                //consultForm.setSErrorMsg("Ocurrio un Error en el Servicio de Subsanacion");
//                return mapping.findForward("ConsultSubSanacionPorta");
//            } 
             // MDP - 19/07/2016 - FIN
            
            //Nueva llamada al Servicio ProcessMessagePortability con SpringWS -- vcedenos@soaint.com
             
            GetProcessMsgPortabilityRequest requestMsgPortability = ofProcessMessagePortability.createGetProcessMsgPortabilityRequest();
            SendMessageBE sendMsgBE = ofProcessMessagePortability.createSendMessageBE();
            sendMsgBE.setStrRemitente("20");
            sendMsgBE.setStrIdProcess(consultForm.getTxtAplicationId());
            sendMsgBE.setStrMessageTypeId("ENVIO");
            sendMsgBE.setStrIdMessage("APD");
            sendMsgBE.setStrPhoneNumber(consultForm.getTxtNumberPhone());
            sendMsgBE.setStrEntityPayment(consultForm.getTxtEntidadPago());
            
            //Aplicamos el formato del nombre de la imagen (YYYMMDD)            
             Calendar now = Calendar.getInstance();
             DecimalFormat mFormat= new DecimalFormat("00");
             
             String  sNameFile = sendMsgBE.getStrRemitente().trim();
             sNameFile= sNameFile + String.valueOf(now.get(Calendar.YEAR)) + String.valueOf(mFormat.format(now.get(Calendar.MONTH)+ 1)) + String.valueOf(mFormat.format(now.get(Calendar.DAY_OF_MONTH)));
            
            //Aplicamos el formato del nombre de la imagen (Correlativo)
             //sNameFile= sNameFile + consultForm.getTxtAplicationId().substring(Math.max(consultForm.getTxtAplicationId().length() - 5, 0));;
              sNameFile= sNameFile.trim() + portabilidadService.getNextValueSequenceSubsa().trim();
            //Aplicamos el formato del nombre de la imagen (Constante de Acreditacion de Pago)
             sNameFile= sNameFile+"-P";
                         
            //Concatenamos la extencion de la imagen
            if(consultForm.getStrFileName()!=null){
                String sNameExt[] = consultForm.getStrFileName().split("\\.");
                sNameFile= sNameFile+ "." + sNameExt[1];
                
                
            }
            //sendMsgBE.setStrAttachmentDoc(consultForm.getStrFileName());
             if(logger.isDebugEnabled()){
                logger.debug("Nombre de la imagen  :"+  sNameFile);
             }
             sendMsgBE.setStrAttachmentDoc(sNameFile);
            
            sendMsgBE.setDocument(compressNewImageInByte);                    
                            
            
            DateFormat originalFormat = new SimpleDateFormat("dd/MM/yyyy");
            DateFormat targetFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            Date date = originalFormat.parse(consultForm.getTxtDateEnd());
            String formattedDate = targetFormat.format(date); 
            if(logger.isDebugEnabled()){
               logger.debug(formattedDate);
               logger.debug("Date : " + formattedDate);
            }

            sendMsgBE.setStrPaymentDate(formattedDate);
            sendMsgBE.setStrPaymentOperationNumber(consultForm.getTxtNumeroOperacion());
            sendMsgBE.setStrDebtAmount(consultForm.getLblMonto());
            sendMsgBE.setStrCurrencyType(consultForm.getLblTipoMoneda());            
            sendMsgBE.setStrOrigin("RETAIL");                
            //sendMsgBE.setstr
            //
            
            AssociatedKey associatedKeyParam = ofProcessMessagePortability.createAssociatedKey();
            
            //Id del item de la orden
            associatedKeyParam.setObjectNumber(String.valueOf(consultForm.getTxtIdDeviceItem().toString()));
            //RTL
            associatedKeyParam.setObjectType("RETAIL");
            
            sendMsgBE.setKey(associatedKeyParam);    
            
            //ANTES DE ENVIAR LA SUBSANACION ACTUALIZAMOS EL DEL ITEM npstatusportability
            String sCodePromoter = null;
            sCodePromoter=(String)request.getSession().getAttribute("CodePromoter");
            
            if(!portabilidadService.updNpStatusPortability(Integer.parseInt(consultForm.getTxtIdDeviceItem().toString()), Integer.parseInt(consultForm.getTxtIdOrderItem().toString()),sCodePromoter)){
               if(logger.isDebugEnabled()){
                  logger.debug("No se Actualizo el item de Portabilidad  :"+ Integer.parseInt(consultForm.getTxtIdDeviceItem().toString())+ " --- "+  Integer.parseInt(consultForm.getTxtIdOrderItem().toString()) );
               }
            }
            
            List<NpPortabilidad> portabilidadSubsa=(List<NpPortabilidad>)request.getSession().getAttribute("ListaPortabilidadSubsanacion");
            
            
            
            requestMsgPortability.setSendMessageBE(sendMsgBE);
            
            if(logger.isDebugEnabled()){
               logger.debug("requestMsgPortability :"+ requestMsgPortability.toString());
            }
            
            //Aca se invoca al nuevo servicio
            GetProcessMsgPortabilityResponse processMessagePortabilityResponse = (GetProcessMsgPortabilityResponse)service.marshalSendAndReceive(requestMsgPortability);
            
            
            int iCodeError = new Integer(processMessagePortabilityResponse.getAudit().getErrorCode()); 

            if(logger.isDebugEnabled()){            
               logger.debug("processMsgPortabilityResponse.getErrorCode :"+ iCodeError);
            }
            
            if (iCodeError==Constant.ERROR_CODE_OK){
            //SE CARGA LAS IMAGENES EN LA TRASIT
             //try {
                 if(logger.isDebugEnabled()){
                    logger.debug("portabilidadSubsa.size() :"+ portabilidadSubsa.size());
                 }
                 String sDocumentType=null;
                 String sDocumentNumber=null;
                 String sCustomerId =null;
                 String sOrderId = null;
                 
                 //SE ACTUALIZA EL ESTADO DEL ITEM DE LA PORTABILIDAD A 0=SUBSANAR,1=SUBSANADO,2=ERRROR
                 if(!portabilidadService.updNpStatusSubsana(Integer.parseInt(consultForm.getTxtIdOrderItem().toString()),
                                                                Integer.parseInt(consultForm.getTxtIdDeviceItem().toString()),
                                                                Constant.SUBSANA_ESTADO_SUBSANADO)){
                     if(logger.isDebugEnabled()){
                          logger.debug("SUBSANACION : No se Actualizo el item de Portabilidad :"+ Integer.parseInt(consultForm.getTxtIdDeviceItem().toString())+ " --- "+  Integer.parseInt(consultForm.getTxtIdOrderItem().toString()));
                     }
                 }
                  
                 for( int x = 0 ; x<portabilidadSubsa.size() ; x++){
                     if (portabilidadSubsa.get(x).getNporderitemdeviceid().equals(consultForm.getTxtIdDeviceItem())){
                         portabilidadSubsa.get(x).setNpstatussubsanacion(1);
                          sOrderId=String.valueOf(portabilidadSubsa.get(x).getIdgenerico().toString());
                         sCustomerId=String.valueOf(portabilidadSubsa.get(x).getCustomerId().toString());
                         if(logger.isDebugEnabled()){
                            logger.debug("set APD :"+ portabilidadSubsa.get(x).getNporderitemdeviceid());
                         }
                     }
                 }   
                 
                 try{                         
                     Boolean imageProcessOk = false;
                     
                     String customerId = String.valueOf(consultForm.getTxtcustomerid());
                     
                     imageProcessOk = gestorImagenes.processOrderImages(sOrderId, String.valueOf(orderPortal), customerId, documentListA);
                     
                     
                         if (imageProcessOk) { 
                     if(logger.isDebugEnabled()){
                             logger.debug("Error: Se inserto correctamente el voaucher de la orden de Subsanación" + imageProcessOk);
                             }
                         }else{
                             if(logger.isDebugEnabled()){
                             logger.debug("Error: No se inserto correctamente el voaucher de la orden de Subsanación" + imageProcessOk);
                         }
                     }
                     
                     
                 } catch(Exception e) {                         
                      logger.error("Error: ", e);
                  }
                                                                                              
            }else{
                request.setAttribute("sErrorMsgRequest","Ocurrio un Error en el Servicio de Subsanacion");
                //consultForm.setSErrorMsg("Ocurrio un Error en el Servicio de Subsanacion");
            }
                
            request.getSession().setAttribute("ListaPortabilidadSubsanacion",portabilidadSubsa);
            //getproces
            //FBERNALES - 26/08/2014/ - INICIALIZAMOS EL FLAG DE BORRADO DE FORMFILE PARA EVITAR LA INSERCION DE 
            //IMAGENES DUPLICADAS.
            consultForm.setiFlagSubmitImg(0);
                             
            //request.getSession().setAttribute("ListaPortabilidadSubsanacion",portabilidad);           
            return mapping.findForward("ConsultSubSanacionPorta");
    }
    
        // MDP - 19/07/2016 - INICIO
	private void validateFile(ConsultForm form) throws Exception{
		INpParameterService parameterService = (INpParameterService) getInstance("NpParameterService");
		List<NpParameter> subsanarOrdenParams = parameterService.getEntityByxNameDomainRefresh(Constant.DOMAIN_SUBSANAR_ORDEN_PORT);	
		String fileName = form.getStrFileName();
		FormFile file = getFormFile(fileName, form);
		if( file != null ){
			NpParameter maxSizeParam = Util.getParameterByName(subsanarOrdenParams, Constant.PARAMETER_SUBSANAR_ORDEN_PORT_FILE_MAX_SIZE);
			NpParameter formatsParam = Util.getParameterByName(subsanarOrdenParams, Constant.PARAMETER_SUBSANAR_ORDEN_PORT_FILE_FORMAT);
			NpParameter messageParam = Util.getParameterByName(subsanarOrdenParams, Constant.PARAMETER_SUBSANAR_ORDEN_PORT_MSG);
			
			String extension = Util.getFileExtension(fileName);
                        if( StringUtils.IsNullorEmpty( formatsParam.getNpparametervalue1()) ) {
                                throw new Exception(messageParam.getNpparametervalue1());
                        }  
                        
			String[] formats = formatsParam.getNpparametervalue1().split(",");
			if( !Util.isValidValue(extension, formats) ) {
				throw new Exception(messageParam.getNpparametervalue1());
			}
                        
                        int maxFileSize = 0;
                        try{
                            maxFileSize = Integer.valueOf(maxSizeParam.getNpparametervalue1());
                        }catch(Exception e){
                            maxFileSize = 0;
                            logger.error(e.getMessage(), e);
                        }
                        if ( file.getFileSize() > maxFileSize ){
                                throw new Exception(messageParam.getNpparametervalue1());
                        }                        
		}
	}
	
	public ActionForward findParametersSubsanacion(ActionMapping mapping, ActionForm form, HttpServletRequest req,  HttpServletResponse resp) throws Exception {
		INpParameterService parameterService = (INpParameterService) getInstance("NpParameterService");
		List<NpParameter> subsanarOrdenParams = parameterService.getEntityByxNameDomainRefresh(Constant.DOMAIN_SUBSANAR_ORDEN_PORT);
		NpParameter flagParam = Util.getParameterByName(subsanarOrdenParams, Constant.PARAMETER_SUBSANAR_ORDEN_PORT_FLAG);
		NpParameter formatsParam = Util.getParameterByName(subsanarOrdenParams, Constant.PARAMETER_SUBSANAR_ORDEN_PORT_FILE_FORMAT);
		NpParameter labelParam = Util.getParameterByName(subsanarOrdenParams, Constant.PARAMETER_SUBSANAR_ORDEN_PORT_LABEL);
		
		Map<String, String> map = new HashMap();
		map.put("FLAG", flagParam.getNpparametervalue1());
		map.put("FORMATS", formatsParam.getNpparametervalue1());
		map.put("MAX_SIZE", labelParam.getNpparametervalue1());
		
                resp.setContentType("text/json");
                resp.setCharacterEncoding("ISO-8859-1");
                resp.getWriter().write(new Gson().toJson(map));
                resp.getWriter().flush();
                return null;
	}
	
	public FormFile getFormFile(String fileName, ConsultForm form){
		FormFile file;
		ArrayList files = (ArrayList) form.getFiles();
		for (int i = 0; i < files.size(); i++) {
			file = (FormFile) files.get(i);
			if ( file != null && form.getFileList(fileName).contains(file.getFileName()) ) {
				return file;
			}
		}
		return null;
	}
	
	// MDP - 19/07/2016 - FIN
    
    private boolean checkFileSize (ConsultForm form) throws Exception {
                                                         
        boolean varReturn=true;
        
        String strRealFilesName= form.getStrFileName();
        
        ArrayList myFiles =(ArrayList) form.getFiles();
        
        try{
            
            INpParameterService parameterService = (INpParameterService)getInstance("NpParameterService");
            //obtenermos la maxima longitud que aceptan las imagenes. 
            //IMAGE_MANUAL_MAX_SIZE                     
            String parameterIdAsText = Constant.P_IMAGE_MAX_SIZE;                     
            NpParameter imageManualMaxSize = parameterService.findById(Long.parseLong(parameterIdAsText)); 
            String MaxLengthImage =imageManualMaxSize.getNpparametervalue1();
                                   
            for(int i=0;i<myFiles.size();i++){
              if  (myFiles.get(i) != null && (form.getFileList(strRealFilesName)).contains(((FormFile)myFiles.get(i)).getFileName())){
                if (  ((FormFile)myFiles.get(i)).getFileSize() > Integer.parseInt(MaxLengthImage) ){
                   if(logger.isDebugEnabled()){
                      logger.debug("Validacion de Size imagenes :: Imagen Name:"+ ((FormFile)myFiles.get(i)).getFileName() + " Size :"+((FormFile)myFiles.get(i)).getFileSize());
                   }
                   varReturn =false;
                   break;
                }
              }
            }
        }
        catch (Exception e) {
            logger.error("Validacion de Size imagenes :: Error", e);
        }             
        return varReturn;
    }

     //ovargas 09/07/14 
    
     public ActionForward consultSalesDiaHome(ActionMapping mapping, 
                                             ActionForm form, 
                                             HttpServletRequest request, 
                                             HttpServletResponse response) throws Exception {
                                              
                                   
         logger.info("Inicio consultSalesDiaHome");
         request.getSession().setAttribute("msgError","");
         return mapping.findForward("NewConsultSalesDiaHome");  
         
     }
    
     public ActionForward downLoadSFTP(ActionMapping mapping, 
                                          ActionForm form, 
                                          HttpServletRequest request, 
                                          HttpServletResponse response) throws Exception {
          
         RetailProperties properties = RetailProperties.instance();
        
        String ip = properties.ip;
        String user = properties.user;
        String pass = properties.pass;
        String port = properties.port;
        String pattern = properties.pattern;
        String rootDirectory = properties.rootDirectory;
        SftpUtil sftp=new SftpUtil();
        
        
         
                  if((ip != null && !ip.trim().equals("")) || (user != null && !user.trim().equals("")) || (pass != null && !pass.trim().equals("")) || 
                 (port != null || !port.trim().equals("")) || (pattern != null || !pattern.trim().trim().equals(""))||(rootDirectory != null || 
                 !rootDirectory.trim().equals(""))){
                     try{
                            if(sftp.conectar(ip,user,pass,port)){
                                String nombreArchivo=sftp.getNombreArcVentas(rootDirectory,pattern);
                                
                                if(nombreArchivo!=null && !nombreArchivo.equals("")){
                                    OutputStream out = response.getOutputStream();
                                    response.setContentType("zip");
                                    response.setHeader("Content-disposition","attachment; filename="+nombreArchivo);
                                    sftp.getFichero("/"+nombreArchivo,out);
                                    out.flush();
                                } else{
                                    request.getSession().setAttribute("msgError","No se ha generado el reporte del día.");
                                    return mapping.findForward("NewConsultSalesDiaHome"); 
                                }
                               
                                sftp.desconectar();
                            }else{
                                request.getSession().setAttribute("msgError","Error al conectar al servidor de archivo.");
                                 return mapping.findForward("NewConsultSalesDiaHome"); 
                            }
                        }catch(Exception e){
                            logger.error("",e);;
                        }
                            
                     return null;
                 
                 } else {
                     request.getSession().setAttribute("msgError","Error al conectar al servidor de archivo.");
                     return mapping.findForward("NewConsultSalesDiaHome"); 
                 }
                    
     }
    
  public ActionForward validateTimeSubsanacion(ActionMapping mapping, ActionForm form, HttpServletRequest request, 
                                                      HttpServletResponse response) throws Exception {
          boolean respuesta;
          String sMessageToShow="";
          try{
            INpParameterService parameterService = (INpParameterService)getInstance("NpParameterService");
            List<NpParameter> parameterList = parameterService.getParameterByDomainNameByParameterNameVal(  Constant.RETAIL_PARAMETER_SIMPLE,
                                                                                                            Constant.HORA_MAXIMA_PORTABILIDAD_SUBSANACION,
                                                                                                            2);  
            NpParameter parameterMax=null;
            if(parameterList!=null){
              for (int i=0;i<parameterList.size();i++){                 
                 parameterMax=parameterList.get(i);
              }
            }
            
            parameterList = parameterService.getParameterByDomainNameByParameterNameVal( Constant.RETAIL_PARAMETER_SIMPLE,
                                                                                         Constant.HORA_MINIMA_PORTABILIDAD_SUBSANACION,
                                                                                         2);  
            NpParameter parameterMin=null;
            if(parameterList!=null){
              for (int i=0;i<parameterList.size();i++){                 
                 parameterMin=parameterList.get(i);
              }
            }
            
            if(parameterMax.getNpstatus().equals("1") && parameterMin.getNpstatus().equals("1")){
              sMessageToShow = parameterMax.getNpparametervalue2();
              INpPortabilidadServices portabilityService = (INpPortabilidadServices)getInstance("NpPortabilidadServices");
              
              Boolean bValidateTimeLimit = portabilityService.getVerifyTimeLimit(Constant.RETAIL_PARAMETER_SIMPLE,
                                                                                 Constant.HORA_MAXIMA_PORTABILIDAD_SUBSANACION,
                                                                                 Constant.HORA_MINIMA_PORTABILIDAD_SUBSANACION,
                                                                                 "");
              if(bValidateTimeLimit) {
                respuesta=false;
              }                                                                               
              else{
                respuesta=true;
              }
            }                                                                                                             
            else{
              respuesta=true;
            }
            
          }catch(Exception e){
            logger.error("", e);
            respuesta=true;
          }
          ResponseBean responseBean=new ResponseBean();
          responseBean.setRespuesta(respuesta);
          responseBean.setMensaje(sMessageToShow);
          response.setContentType("text/json");
          response.setCharacterEncoding("ISO-8859-1");
          response.getWriter().write(new Gson().toJson(responseBean));
          response.getWriter().flush();
          return null;
      }
     
     //ovargas 20/08/15
     
      public ActionForward saleDetailConsult(ActionMapping mapping, ActionForm form, 
                                      HttpServletRequest request, 
                                      HttpServletResponse response) throws Exception {

                logger.info("Inicio saleDetail");
                ConsultForm consultForm = (ConsultForm)form;
                Long orderChoose = consultForm.getOrderchoose();
                //Detalle Venta Cabecera
                INpOrderItemService iNpOrderItemService = (INpOrderItemService) getInstance("NpOrderItemService"); //lvalencia
                SaleListResp orderDetail = null;
                orderDetail = iNpOrderItemService.getOrderDetailList(orderChoose.intValue());
                
                int k = 0;                
                if (orderDetail != null && orderDetail.getLista().size() > k) {
                    //BeanConsultSaleSearch temporal = (BeanConsultSaleSearch)saleDetailLst.get(k);
                        SaleBean orderDetailBean = orderDetail.getLista().get(k);
                        String fechaHora = "";
                        if(orderDetailBean.getOrderDate()!= null){                            
                            DateFormat mascara = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            fechaHora = mascara.format(orderDetailBean.getOrderDate());
                        }                        
                        consultForm.setTxtDateSale(fechaHora);
                        consultForm.setTxtRetailer(orderDetailBean.getRetailer());
                        consultForm.setTxtStore(orderDetailBean.getStore());
                        consultForm.setTxtPOS(orderDetailBean.getPos());
                        consultForm.setTxtPrometer(orderDetailBean.getPromoter());
                        consultForm.setTxtTypePayment(orderDetailBean.getPaymentType());
                        consultForm.setTxtNumberVoucher(orderDetailBean.getVoucher());
                        consultForm.setTxtSoluction(orderDetailBean.getSolution());
                        consultForm.setTxtCustomerName(orderDetailBean.getName());
                        consultForm.setTxtOrderNumber(orderDetailBean.getOrderNumber());
                        consultForm.setTxtOrderStatus(orderDetailBean.getOrderStatus());
                }
                // FIN Detalle Venta Cabecera
                 ArrayList listaOrdenes = null;
                 listaOrdenes = obtenerOrdenesById(orderChoose.intValue());
                 request.getSession().setAttribute("consultSaleDetail", listaOrdenes);
                logger.info("Fin saleDetail");
                return mapping.findForward("SaleDetail");
  }
    
    //lvalencia
    private ArrayList armarListaAsistencia(List<NpPromoter> listaEntradasItems, List<NpPromoter> listaSalidasItems) {
        ArrayList listaFinal = new ArrayList();

        for (int i = 0; i < listaEntradasItems.size(); i++) {
            BeanConsultAttendanceSearch itemAsistencia = 
                new BeanConsultAttendanceSearch();
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
            itemAsistencia.setAD_NPDATE(format.format(listaEntradasItems.get(i).getInitialDate()));
            itemAsistencia.setAV_NPMOVTYPE("Ingreso");
            itemAsistencia.setAV_NPPOSNAME(listaEntradasItems.get(i).getNpposname());
            itemAsistencia.setAV_NPPROMOTERNAME(listaEntradasItems.get(i).getNppromotername());
            itemAsistencia.setAV_NPRETAILERNAME(listaEntradasItems.get(i).getNpretailname());
            itemAsistencia.setAV_NPSTORENAME(listaEntradasItems.get(i).getNpstorename());

            listaFinal.add(itemAsistencia);
        }

        for (int i = 0; i < listaSalidasItems.size(); i++) {
            BeanConsultAttendanceSearch itemAsistencia = 
                new BeanConsultAttendanceSearch();
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
            itemAsistencia.setAD_NPDATE(format.format(listaSalidasItems.get(i).getFinalDate()));
            itemAsistencia.setAV_NPMOVTYPE("Salida");
            itemAsistencia.setAV_NPPOSNAME(listaSalidasItems.get(i).getNpposname());
            itemAsistencia.setAV_NPPROMOTERNAME(listaSalidasItems.get(i).getNppromotername());
            itemAsistencia.setAV_NPRETAILERNAME(listaSalidasItems.get(i).getNpretailname());
            itemAsistencia.setAV_NPSTORENAME(listaSalidasItems.get(i).getNpstorename());

            listaFinal.add(itemAsistencia);
        }
        return listaFinal;
    }
    
}
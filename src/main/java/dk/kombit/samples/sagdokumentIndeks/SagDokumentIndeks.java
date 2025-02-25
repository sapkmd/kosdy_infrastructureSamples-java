package dk.kombit.samples.sagdokumentIndeks;

import dk.kombit.samples.klassifikation.Klasse;
import dk.kombit.samples.organisation.Organisation;
import dk.kombit.samples.organisation.Virksomhed;
import dk.kombit.samples.utils.ClientProperties;
import dk.kombit.samples.utils.SoapUtils;
import dk.kombit.xml.schemas.requestheader._1.RequestHeaderType;
import dk.serviceplatformen.xml.schemas.serviceplatformfault._1.ServiceplatformFault;
import dk.serviceplatformen.xml.wsdl.soap11.sagdokument.sagdokumentindeks._5.FjernRequestType;
import dk.serviceplatformen.xml.wsdl.soap11.sagdokument.sagdokumentindeks._5.FjernResponseType;
import dk.serviceplatformen.xml.wsdl.soap11.sagdokument.sagdokumentindeks._5.FremsoegRequestType;
import dk.serviceplatformen.xml.wsdl.soap11.sagdokument.sagdokumentindeks._5.FremsoegResponseType;
import dk.serviceplatformen.xml.wsdl.soap11.sagdokument.sagdokumentindeks._5.ImporterRequestType;
import dk.serviceplatformen.xml.wsdl.soap11.sagdokument.sagdokumentindeks._5.ImporterResponseType;
import dk.serviceplatformen.xml.wsdl.soap11.sagdokument.sagdokumentindeks._5.SagDokumentIndeksPortType;
import dk.serviceplatformen.xml.wsdl.soap11.sagdokument.sagdokumentindeks._5.SagDokumentIndeksService;
import oio.sagdok._3_0.AktoerTypeKodeType;
import oio.sagdok._3_0.LivscyklusKodeType;
import oio.sagdok._3_0.StandardReturType;
import oio.sagdok._3_0.TidspunktType;
import oio.sagdok._3_0.UnikIdType;
import oio.sagdok._3_0.UnikReturType;
import oio.sagdok._3_0.VirkningType;
import oio.sts.generelledefinitioner._1_2_4.LokalUdvidelseListeType;
import oio.sts.generelledefinitioner._1_2_4.RelationType;
import oio.sts.sagdok.sag._1_2_4.AttributListeType;
import oio.sts.sagdok.sag._1_2_4.EgenskaberType;
import oio.sts.sagdok.sag._1_2_4.FremdriftStatusKodeType;
import oio.sts.sagdok.sag._1_2_4.FremdriftType;
import oio.sts.sagdok.sag._1_2_4.RegistreringType;
import oio.sts.sagdok.sag._1_2_4.RelationListeType;
import oio.sts.sagdok.sag._1_2_4.SagType;
import oio.sts.sagdok.sag._1_2_4.TilstandListeType;
import oio.sts.sagdok.sagdokumentindeks._1_2_4.FjernSagDokumentIndeksInputType;
import oio.sts.sagdok.sagdokumentindeks._1_2_4.FremsoegFilterSagDokumentIndeksInputType;
import oio.sts.sagdok.sagdokumentindeks._1_2_4.FremsoegSagDokumentIndeksInputType;
import oio.sts.sagdok.sagdokumentindeks._1_2_4.ImporterSagDokumentIndeksInputType;
import oio.sts.sagdok.sagdokumentindeks._1_2_4.SagVisFilterType;
import oio.sts.sagdok.sagdokumentindeks._1_2_4.SagVisType;
import oio.sts.sagdok.sagindeks._1_2_4.FoelsomhedType;
import oio.sts.sagdok.sagindeks._1_2_4.SagsaktoerLokalUdvidelseType;
import oio.sts.sagdok.sagindeks._1_2_4.SagsitsystemRelationType;
import oio.sts.sagdok.sagindeks._1_2_4.SagsklasseLokalUdvidelseType;
import oio.sts.sagdok.sagindeks._1_2_4.SagspartLokalUdvidelseType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;
import java.util.List;

/**
 * Class for handling requests to SagDokumentIndeks
 */
public class SagDokumentIndeks {

    private static final Logger LOGGER = LoggerFactory.getLogger(SagDokumentIndeks.class);

    private final SagDokumentIndeksPortType sagDokumentIndeksPort;

    public SagDokumentIndeks() {
        sagDokumentIndeksPort = new SagDokumentIndeksService().getSagDokumentIndeksPort();
        BindingProvider bindingProvider = (BindingProvider) sagDokumentIndeksPort;
        bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, ClientProperties.getInstance().getSagdokumentIndeksEndpointUrl());
    }

    private static SagDokumentIndeks sagDokumentIndeks;

    /**
     * Methods that allow other classes to get an instance of SagdokumentIndeks
     * to avoid creating multiple instances of the same class
     * If an instance of SagDokumentIndeks does not exist, one is created
     *
     * @return Instance of sagDokumentIndeks
     */
    public static SagDokumentIndeks getSagDokumentIndeks()
    {
        if (sagDokumentIndeks == null)
            sagDokumentIndeks = new SagDokumentIndeks();
        return sagDokumentIndeks;
    }

    /**
     * Imports a case to the SagDokumentIndeks
     * ses input variables defined in ConfigVariables.
     *
     * @param sagUuid The UUID of the imported case
     * @return Status code and status text to the console
     */
    public void importer(String sagUuid) {

        // Class instances
        Virksomhed virksomhed = Virksomhed.getVirksomhed();
        Organisation organisation = Organisation.getOrganisation();
        Klasse klassifikation = Klasse.getKlassifikation();

        // Preparing the import

        // The case must be related to an Organisation object in Fælleskommunalt Organisationssystem.

        // We want to find the Organisation object based on the CVR-number of the test municipality.
        // First we need to find the Virksomhed object in Fælleskommunalt Organisationssystem which holds the CVR
        var virksomhedUuid = virksomhed.getVirksomhedUuid(ClientProperties.getInstance().getMyndighedCvr());

        // Next we need to find the Organisation object related to the Virksomhed object.
        // The UUID of the Organisation object will be used when we relate the case to the municipality organisation.
        var org = organisation.getOrganisationUuidName(virksomhedUuid);
        var organisationUuid = org.getFirst();
        var organisationNavn = org.getSecond();

        // The case must be related to a Klasse object in Fælleskommunalt Klassifikationssystem which represents a KLE Emne
        // We want to find the Klasse object based on the name (brugervendt nøgle) of the KLE Emne.

        var emne = klassifikation.getKlasseUuidName(ClientProperties.getInstance().getKleKlasse());
        var emneUuid = emne.getFirst();
        var emneKlasseTitel = emne.getSecond();

        // The case must be related to a Klasse object in Fælleskommunalt Klassifikationssystem which represents a KLE Handlingsfacet
        // We want to find the Klasse object based on the name (brugervendt nøgle) of the KLE Handlingsfacet.

        var handlingsfacet = klassifikation.getKlasseUuidName(ClientProperties.getInstance().getKleHandlingsfacet());
        var facetUuid = handlingsfacet.getFirst();
        var facetKlasseTitel = handlingsfacet.getSecond();

        // Write the variables to the console
        System.out.println("\nPreparations before importing the case...\n");
        System.out.println("Myndighed CVR: " + ClientProperties.getInstance().getMyndighedCvr() +
                "\n *Virksomhed UUID: " + virksomhedUuid + "\n *Organisation UUID: " + organisationUuid + "\n *Organisation Navn: " + organisationNavn + "\n");
        System.out.println("KLE emne: " + ClientProperties.getInstance().getKleKlasse() +
                "\n *Klasse UUID: " + emneUuid + "\n *Klassetitel: " + emneKlasseTitel + "\n");


        System.out.println("KLE handlingsfacet: " + ClientProperties.getInstance().getKleHandlingsfacet() +
                "\n *Klasse UUID: " + facetUuid + "\n *Klassetitel: " + facetKlasseTitel + "\n");

        // Now we are ready to import the case

        ImporterRequestType importerRequest = new ImporterRequestType().withImporterSagDokumentIndeksInput(
                new ImporterSagDokumentIndeksInputType().withDokumentIndeksOrSagIndeks(
                        new SagType().withUUIDIdentifikator(sagUuid)
                                .withRegistrering(
                                        new RegistreringType().withTidspunkt(SoapUtils.getXmlCalender(ClientProperties.getInstance().getSagTidspunkt()))
                                                .withLivscyklusKode(LivscyklusKodeType.IMPORTERET)
                                                .withBrugerRef(
                                                        new UnikIdType().withUUIDIdentifikator(ClientProperties.getInstance().getAktoerRef())
                                                ).withAttributListe(
                                                new AttributListeType().withEgenskaber(
                                                        new EgenskaberType().withBrugervendtNoegle(ClientProperties.getInstance().getSagsNummer())
                                                                .withSagsnummer(ClientProperties.getInstance().getSagsNummer())
                                                                .withTitel(ClientProperties.getInstance().getSagsTitel())
                                                                .withVirkning(createVirkningNowToEternity(ClientProperties.getInstance().getAktoerRef()))
                                                ).withLokalUdvidelseListe(
                                                        new LokalUdvidelseListeType().withAny(
                                                                new oio.sts.sagdok.sagindeks._1_2_4.EgenskaberType().withFoelsomhed(
                                                                        FoelsomhedType.IKKE_FORTROLIGE_DATA
                                                                ).withVirkning(createVirkningNowToEternity(ClientProperties.getInstance().getAktoerRef()))
                                                        )
                                                )
                                        ).withTilstandListe(
                                                new TilstandListeType().withFremdrift(
                                                        new FremdriftType().withFremdriftStatusKode(FremdriftStatusKodeType.OPSTAAET)
                                                                .withVirkning(
                                                                        createVirkning(ClientProperties.getInstance().getAktoerRef(),
                                                                                SoapUtils.getXmlCalender(ClientProperties.getInstance().getSagOprettet()),
                                                                                SoapUtils.getXmlCalender(ClientProperties.getInstance().getSagLukket()))
                                                                ),
                                                        new FremdriftType().withFremdriftStatusKode(FremdriftStatusKodeType.AFSLUTTET)
                                                                .withVirkning(
                                                                        createVirkning(ClientProperties.getInstance().getAktoerRef(),
                                                                                SoapUtils.getXmlCalender(ClientProperties.getInstance().getSagLukket()), null)
                                                                )
                                                )
                                        ).withRelationListe(
                                                new RelationListeType().withSagsaktoer(
                                                        // The case owner (Ejer)
                                                        createRelationWithUdvidelse(
                                                                ClientProperties.getInstance().getEjerRolleUuid(), // Constant
                                                                ClientProperties.getInstance().getEjerOrganisationTypeUuid(), // Constant
                                                                organisationUuid, // Test data
                                                                null,
                                                                ClientProperties.getInstance().getAktoerRef(),
                                                                new SagsaktoerLokalUdvidelseType().withCVRNummer(ClientProperties.getInstance().getMyndighedCvr())
                                                                        .withFuldtNavn(ClientProperties.getInstance().getMyndighedNavn())
                                                        ),
                                                        // The case responsible (Ansvarlig)
                                                        createRelationWithUdvidelse(
                                                                ClientProperties.getInstance().getAnsvarligRolleUuid(), // Constant
                                                                ClientProperties.getInstance().getAnsvarligOrganisationsenhedTypeUuid(), // Constant
                                                                ClientProperties.getInstance().getAktoerRef(), // Test data
                                                                null,
                                                                ClientProperties.getInstance().getAktoerRef(),
                                                                new SagsaktoerLokalUdvidelseType().withCVRNummer(ClientProperties.getInstance().getMyndighedCvr())
                                                                        .withFuldtNavn(ClientProperties.getInstance().getOrganisationsEnhedNavn())
                                                        ),
                                                        // The case primary case worker (Primaer Behandler)
                                                        createRelationWithUdvidelse(
                                                                ClientProperties.getInstance().getPrimaerBehandlerRolleUuid(), // Constant
                                                                ClientProperties.getInstance().getBrugerTypeUuid(), // Constant
                                                                ClientProperties.getInstance().getPrimaerBehandlerUuid(), // Test data
                                                                null,
                                                                ClientProperties.getInstance().getAktoerRef(),
                                                                new SagsaktoerLokalUdvidelseType().withCVRNummer(ClientProperties.getInstance().getMyndighedCvr())
                                                                        .withFuldtNavn(ClientProperties.getInstance().getPrimaerBehandlerNavn())
                                                        )
                                                ).withSagspart(
                                                        // The primary part (Primaer part)
                                                        createRelationWithUdvidelse(
                                                                ClientProperties.getInstance().getPrimaerPartRolleUuid(), // Constant
                                                                ClientProperties.getInstance().getPersonTypeUuid(), // Constant
                                                                null,
                                                                ClientProperties.getInstance().getPrimaerPartCpr(), // Test data
                                                                ClientProperties.getInstance().getAktoerRef(),
                                                                new SagspartLokalUdvidelseType().withFuldtNavn(ClientProperties.getInstance().getPrimaerPartNavn())
                                                        )
                                                ).withSagsklasse(
                                                        // The primary class (primaer klasse)
                                                        createRelationWithUdvidelse(
                                                                ClientProperties.getInstance().getPrimaerKlasseRolleUuid(), // Constant
                                                                ClientProperties.getInstance().getKlasseTypeUuid(), // Constant
                                                                emneUuid, // Test data
                                                                null,
                                                                ClientProperties.getInstance().getAktoerRef(),
                                                                new SagsklasseLokalUdvidelseType().withBrugervendtNoegle(ClientProperties.getInstance().getKleKlasse())
                                                                        .withFacettitel(ClientProperties.getInstance().getKlasseFacetTitel())
                                                                        .withKlassetitel(emneKlasseTitel)
                                                        ),
                                                        // The Handlingsklasse (Handlingsklasse)
                                                        createRelationWithUdvidelse(
                                                                ClientProperties.getInstance().getHandlingsklasseRolleUuid(), // Constant
                                                                ClientProperties.getInstance().getKlasseTypeUuid(), // Constant
                                                                facetUuid, // Test data
                                                                null,
                                                                ClientProperties.getInstance().getAktoerRef(),
                                                                new SagsklasseLokalUdvidelseType().withBrugervendtNoegle(ClientProperties.getInstance().getKleHandlingsfacet())
                                                                        .withFacettitel(ClientProperties.getInstance().getHandlingsKlasseFacetTitel())
                                                                        .withKlassetitel(facetKlasseTitel)
                                                        )
                                                ).withSagsarkiv(
                                                        //The archive (Behandlingsarkiv)
                                                        createRelation(
                                                                ClientProperties.getInstance().getBehandlingsarkivRolleUuid(), // Constant
                                                                ClientProperties.getInstance().getArkivTypeUuid(), // Constant
                                                                ClientProperties.getInstance().getAnvenderSystemUuid(), // IT-system data
                                                                null,
                                                                ClientProperties.getInstance().getAktoerRef()
                                                        )
                                                ).withLokalUdvidelseListe(
                                                        //The master IT-system (IT-System Master)
                                                        new LokalUdvidelseListeType().withAny(
                                                                new SagsitsystemRelationType().withRolle(
                                                                        new UnikIdType().withUUIDIdentifikator(
                                                                                ClientProperties.getInstance().getMasterUuid())
                                                                ).withType(
                                                                        new UnikIdType().withUUIDIdentifikator(
                                                                                ClientProperties.getInstance().getItSystemTypeUuid())
                                                                ).withReferenceID(
                                                                        new UnikIdType().withUUIDIdentifikator(ClientProperties.getInstance().getAnvenderSystemUuid())
                                                                ).withVirkning(
                                                                        createVirkningNowToEternity(ClientProperties.getInstance().getAktoerRef())
                                                                ).withSystemNavn(ClientProperties.getInstance().getAnvenderSystemNavn()),
                                                                //The sender IT-system (IT-System Afsender)
                                                                new SagsitsystemRelationType().withRolle(
                                                                        new UnikIdType().withUUIDIdentifikator(
                                                                                ClientProperties.getInstance().getAfsenderTypeUuid())
                                                                ).withType(
                                                                        new UnikIdType().withUUIDIdentifikator(
                                                                                ClientProperties.getInstance().getItSystemTypeUuid())
                                                                ).withReferenceID(
                                                                        new UnikIdType().withUUIDIdentifikator(ClientProperties.getInstance().getAnvenderSystemUuid())
                                                                ).withVirkning(
                                                                        createVirkningNowToEternity(ClientProperties.getInstance().getAktoerRef())
                                                                ).withSystemNavn(ClientProperties.getInstance().getAnvenderSystemNavn())
                                                        )
                                                )
                                        )
                                )
                )
        );

        Holder<RequestHeaderType> requestHeader = SoapUtils.getRequestHeader();

        ImporterResponseType importerResponse;
        try {
            importerResponse = sagDokumentIndeksPort.importer(requestHeader, importerRequest);
        } catch (ServiceplatformFault serviceplatformFault) {
            SoapUtils.logError(serviceplatformFault, LOGGER);
            return;
        }

        System.out.println("Importing Case...");
        logUnikResponses(importerResponse.getImporterSagDokumentIndeksOutput().getUnikRetur());
        logStandardResponses(importerResponse.getImporterSagDokumentIndeksOutput().getStandardRetur());
    }

    /**
     * Searches for a case in the SagDokumentIndeks
     * Outputs status code and status text
     * Outputs case number if a case is found in the SagDokumentIndeks
     *
     * @param uuid
     */
    public void fremsoeg(String uuid) {
        FremsoegRequestType fremsoegRequest = new FremsoegRequestType().withFremsoegSagDokumentIndeksInput(
                new FremsoegSagDokumentIndeksInputType().withSagUuid(uuid).withFilter(
                        // The filter allows us to see egenskaber of the case (Sag) which mean we can get the case number below
                        new FremsoegFilterSagDokumentIndeksInputType().withDokumentVisOrSagVisOrDokumentInkluder(
                                new SagVisType().withVis(
                                        SagVisFilterType.EGENSKABER
                                )
                        )
                )
        );

        Holder<RequestHeaderType> requestHeader = SoapUtils.getRequestHeader();

        FremsoegResponseType fremsoegResponse;
        try {
            fremsoegResponse = sagDokumentIndeksPort.fremsoeg(requestHeader, fremsoegRequest);
        } catch (ServiceplatformFault serviceplatformFault) {
            SoapUtils.logError(serviceplatformFault, LOGGER);
            return;
        }

        System.out.println("Searching for case by UUID...");
        logStandardResponse(fremsoegResponse.getFremsoegSagDokumentIndeksOutput().getStandardRetur());
        // The case number (Sagsnummer) is only printed if a case is found
        if(fremsoegResponse.getFremsoegSagDokumentIndeksOutput().getAntal() != null
                && fremsoegResponse.getFremsoegSagDokumentIndeksOutput().getAntal().size() > 0)
        {
            // The case number (Sagsnummer) is retrieved from the returned XML
            String sagsNummer = fremsoegResponse.getFremsoegSagDokumentIndeksOutput().getSagFiltreretOejebliksbillede().get(0).getRegistrering().get(0).
                    getAttributListe().getEgenskaber().get(0).getSagsnummer();
            System.out.println(" *Case Number: " + sagsNummer + "\n");
        }
    }

    /**
     * Removes a case in the SagDokumentIndeks
     * Outputs status code and status text
     *
     * @param uuid
     */
    public void fjern(String uuid) {
        FjernRequestType fjernRequest = new FjernRequestType().withFjernSagDokumentIndeksInput(
                new FjernSagDokumentIndeksInputType().withSagUuid(
                        new UnikIdType().withUUIDIdentifikator(uuid)
                )
        );

        Holder<RequestHeaderType> requestHeader = SoapUtils.getRequestHeader();

        FjernResponseType fjernResponse;
        try {
            fjernResponse = sagDokumentIndeksPort.fjern(requestHeader, fjernRequest);
        } catch (ServiceplatformFault serviceplatformFault) {
            SoapUtils.logError(serviceplatformFault, LOGGER);
            return;
        }

        System.out.println("Removing case...");
        logStandardResponses(fjernResponse.getFjernSagDokumentIndeksOutput().getStandardRetur());
        logUnikResponses(fjernResponse.getFjernSagDokumentIndeksOutput().getUnikRetur());
    }

    /**
     * Helper method for printing log responses
     *
     * @param responses
     */

    private void logStandardResponses(List<StandardReturType> responses) {
        for (var response : responses) {
            logStandardResponse(response);
        }
    }

    /**
     * Helper method for printing log responses
     *
     * @param responses
     */
    private void logUnikResponses(List<UnikReturType> responses) {
        for (var response : responses) {
            logUnikResponse(response);
        }
    }

    /**
     * Method for printing standard logresponses
     *
     * @param response
     */
    private void logStandardResponse(StandardReturType response) {
        System.out.println(" *Status: " + response.getStatusKode());
        System.out.println(" *Statustext: " + response.getFejlbeskedTekst() + "\n");
    }

    /**
     * Method for printing unique logresponses
     *
     * @param response
     */
    private void logUnikResponse(UnikReturType response) {
        System.out.println(" *Status: " + response.getStatusKode());
        System.out.println(" *Statustext: " + response.getFejlbeskedTekst() + "\n");
    }

    /**
     * Creates a relation with a LokalUdvidelse
     *
     * @param rolleUuid The UUID of the role
     * @param typeUuid The UUID of the type
     * @param referenceUuid The UUID of the reference. Should be null if the reference is by URN!
     * @param referenceUrn The URN of the reference. Should be null if the reference is by UUID!
     * @param virkningUuid The UUID to pass on to the Virkning which is the case registrar
     * @param udvidelse The LokalUdvidelse
     * @return relation with a LokalUdvidelse
     */
    private RelationType createRelationWithUdvidelse(
            String rolleUuid,
            String typeUuid,
            String referenceUuid,
            String referenceUrn,
            String virkningUuid,
            Object udvidelse
    ) {
        return createRelation(rolleUuid, typeUuid, referenceUuid, referenceUrn, virkningUuid).withLokalUdvidelseListe(
                new LokalUdvidelseListeType().withAny(udvidelse)
        );
    }

    /**
     * Creates a relation
     *
     * @param rolleUuid The UUID of the role
     * @param typeUuid The UUID of the type
     * @param referenceUuid The UUID of the reference. Should be null if the reference is by URN!
     * @param referenceUrn The URN of the reference. Should be null if the reference is by UUID!
     * @param virkningUuid The UUID to pass on to the Virkning which is the case registrar
     * @return relation
     */
    private RelationType createRelation(String rolleUuid, String typeUuid, String referenceUuid, String referenceUrn, String virkningUuid) {
        return new RelationType().withRolle(
                new UnikIdType().withUUIDIdentifikator(rolleUuid)
        ).withType(
                new UnikIdType().withUUIDIdentifikator(typeUuid)
        ).withReferenceID(
                referenceUuid != null ? new UnikIdType().withUUIDIdentifikator(referenceUuid) : new UnikIdType().withURNIdentifikator(referenceUrn)
        ).withVirkning(createVirkningNowToEternity(virkningUuid));
    }

    /**
     * Creates a Virkning that starts now and never ends
     *
     * @param uuid The UUID of the case registrar
     * @return VirkningFromNowToEternity
     */
    private VirkningType createVirkningNowToEternity(String uuid) {
        return new VirkningType().withAktoerRef(
                new UnikIdType().withUUIDIdentifikator(uuid)
        ).withFraTidspunkt(
                new TidspunktType().withTidsstempelDatoTid(SoapUtils.getXmlCalender())
        ).withTilTidspunkt(
                new TidspunktType().withGraenseIndikator(true)
        ).withAktoerTypeKode(AktoerTypeKodeType.BRUGER);
    }

    /**
     * Creates a Virkning
     *
     * @param uuid The UUID of the case registrar
     * @return Virkning
     */
    private VirkningType createVirkning(String uuid, XMLGregorianCalendar fraTidspunkt, XMLGregorianCalendar tilTidspunkt) {
        return new VirkningType().withAktoerRef(
                new UnikIdType().withUUIDIdentifikator(uuid)
        ).withFraTidspunkt(
                new TidspunktType().withTidsstempelDatoTid(fraTidspunkt)
        ).withTilTidspunkt(
                tilTidspunkt != null ? new TidspunktType().withTidsstempelDatoTid(tilTidspunkt) : new TidspunktType().withGraenseIndikator(true)
        ).withAktoerTypeKode(AktoerTypeKodeType.BRUGER);
    }
}

package fr.pylsoft.doclet;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.spi.ToolProvider;
import java.util.stream.Collectors;

import com.automation.xmldoclet.util.DocletOption;
import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.ParamTree;
import com.sun.source.util.DocTrees;
import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Cucumber implements Doclet {
    private static final Pattern SAUT_DE_LIGNE = Pattern.compile("\\n" );

    private static final String DEPRECATED = "Deprecated";
    private static final String EXAMPLE = "example";

    private static final List<String> ANNOTATIONS_INCLUSES = new ArrayList<>();

    private static final Map<String, Integer> MAP_ANNOTATIONS_TROUVEES = new HashMap<>();

    private static String name;
    private static String fullPath;
    private static String projectName;
    private static String xslTxt;
    private static String xslHtml;
    private static boolean outputToXml;
    private static boolean outputToHtml;
    private static boolean outputToTxt;
    private static String transformers;

    @Override
    public void init(Locale locale, Reporter reporter) {
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public Set<? extends Option> getSupportedOptions() {
        return new LinkedHashSet<>(
                List.of(DocletOption.ofParameter(
                                List.of(CucumberOption.NAME),
                                "name",
                                "XSL HTML filepath\\nDefault: .",
                                argument -> xslTxt = argument
                        ),
                        DocletOption.ofParameter(
                                List.of(CucumberOption.XSL_HTML),
                                "xsloutputToHtm",
                                "XSL HTML filepath\\nDefault: .",
                                argument -> xslHtml = argument
                        ),
                        DocletOption.ofParameter(
                                List.of(CucumberOption.XSL_TXT),
                                "xslTxtFileDest",
                                "XSL TXT filepath\\nDefault: .",
                                argument -> xslTxt = argument
                        ),
                        DocletOption.ofFlag(
                                List.of(CucumberOption.XML),
                                "Output to xml\\nDefault: true.",
                                () -> outputToXml = true
                        ),
                        DocletOption.ofFlag(
                                List.of(CucumberOption.HTML),
                                "Output to html\\nDefault: false.",
                                () -> outputToHtml = true
                        ),
                        DocletOption.ofFlag(
                                List.of(CucumberOption.TXT),
                                "Output to txt\\nDefault: false.",
                                () -> outputToTxt = true
                        ),
                        DocletOption.ofParameter(
                                List.of(CucumberOption.PROJET),
                                "name",
                                "FILE NAME\\nDefault: JavadocCucumber.",
                                argument -> projectName = argument
                        ),
                        DocletOption.ofParameter(
                                List.of(CucumberOption.OUT),
                                "fullPath",
                                "FULL PATH\\nDefault: .",
                                argument -> fullPath = argument
                        ),
                        DocletOption.ofParameter(
                                List.of(CucumberOption.TRANSFORMERS),
                                "transformers",
                                "Trnsformers\\nDefault: .",
                                argument -> transformers = argument
                        )
                )
        );
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public boolean run(DocletEnvironment environment) {
        return start(environment);
    }

    static class TAG_XML {
        static final String RACINE = "JAVADOC";
        static final String CLASSE = "CLASSE";
        static final String FONCTION = "FONCTION";
        static final String ANNOTATION = "ANNOTATION";
        static final String COMMENTAIRE = "COMMENTAIRE";
        static final String PARAM = "PARAM";
        static final String PHRASE = "PHRASE";
        static final String LIGNE = "LIGNE";
        static final String TAG = "TAG";
        static final String RESUME = "RESUME";
    }

    static class ATTRIBUT_XML {
        public static final String PROJET = "projet";
        static final String VERSION = "docletVersion";
        static final String DATE = "date";
        static final String NOM = "nom";
        static final String PHRASE = "phrase";
        static final String VALUE = "value";
        static final String TYPE = "type";
        static final String NOM_PARAMETRE = "nomParametre";
        static final String DEPRECATED = "Deprecated";
        static final String NOMBRE_PHRASE = "nbPhrases";
    }

    public static void main(String[] args) {
        ToolProvider toolProvider = ToolProvider.findFirst("javadoc" ).or(() -> {
            System.out.println("Loading of ToolProvider failed. Exit now." );
            System.exit(1);
            return Optional.empty();
        }).orElseThrow();

        if (args != null && args.length > 0) {
            toolProvider.run(System.out, System.err, args);
        } else {
            toolProvider.run(System.out, System.err, //
                    "-doclet", Cucumber.class.getName(),
                    "-classpath", "/home/aleksei/.m2/repository/io/cucumber/cucumber-java/4.2.0/cucumber-java-4.2.0.jar:/home/aleksei/.m2/repository/com/automation/xml-doclet/2.0.0/xml-doclet-2.0.0.jar:/home/aleksei/.m2/repository/org/seleniumhq/selenium/selenium-api/2.53.0/selenium-api-2.53.0.jar:/home/aleksei/.m2/repository/org/seleniumhq/selenium/selenium-support/2.53.0/selenium-support-2.53.0.jar:/home/aleksei/.m2/repository/io/cucumber/cucumber-core/4.5.2/cucumber-core-4.5.2.jar", //
                    "-docletpath", ".", //
                    "-encoding", "UTF-8", //
                    "-sourcepath", "/home/aleksei/dev/cucumber-doclet/src/main/java", //
                    CucumberOption.XSL_HTML, "/home/aleksei/dev/cucumber-doclet/src/main/resources/doc/DocCucumberToHtml.xsl", //
                    CucumberOption.XSL_TXT, "/home/aleksei/dev/cucumber-doclet/src/main/resources/doc/DocCucumberToTexte.xsl", //
                    CucumberOption.XML, //
//                    CucumberOption.HTML, //
//                    CucumberOption.TXT, //
                    "/home/aleksei/dev/cucumber-doclet/src/main/java/fr/pylsoft/doclet/ExampleSteps.java" //
            );
        }
    }

    /**
     * @param docletEnvironment Document racine contenant le résultat du traitement réalisé par javadoc.exe
     * @return true si traitement ok sinon false
     */
    public static boolean start(DocletEnvironment docletEnvironment) {
        try {
            ANNOTATIONS_INCLUSES.addAll(Util.recupererListeAnnotationsCucumber());
            ANNOTATIONS_INCLUSES.addAll(Collections.singletonList(DEPRECATED));

            // ne pas oublier l'annotation @Deprecated

            createDocumentXml(docletEnvironment);
        } catch (DocletCucumberException e) {
            System.out.println(e.getMessage());
            return false;
        }

        return true;
    }

    private static void createDocumentXml(final DocletEnvironment docletEnvironment) throws DocletCucumberException {

        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            final DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            Element elementRacine = document.createElement(TAG_XML.RACINE);
            elementRacine.setAttribute(ATTRIBUT_XML.DATE, LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm" )));
            elementRacine.setAttribute(ATTRIBUT_XML.VERSION, Cucumber.class.getPackage().getImplementationVersion());
            if (projectName != null) {
                elementRacine.setAttribute(ATTRIBUT_XML.PROJET, projectName);
            }
            document.appendChild(elementRacine);

            DocTrees docTrees = docletEnvironment.getDocTrees();
            docletEnvironment.getIncludedElements().stream()
                    .filter(element -> element.getKind() == ElementKind.CLASS || element.getKind() == ElementKind.INTERFACE)
                    .map(element -> (TypeElement) element)
                    .forEach(element -> {
                        Element elm = parseClass(docTrees, document, element);
                        if (elm != null) {
                            elementRacine.appendChild(elm);
                        }
                    });

            renseignerAnnotationsTrouveesDansDocument(document, elementRacine);

            creerFichiersSorties(document);
        } catch (
                ParserConfigurationException e) {
            throw new DocletCucumberException("Erreur durant la récupération de la configuration du Doclet", e);
        }

    }

    private static void renseignerAnnotationsTrouveesDansDocument(final Document document, final Element elementRacine) {
        if (MAP_ANNOTATIONS_TROUVEES.isEmpty()) {
            return;
        }

        Element elementResume = document.createElement(TAG_XML.RESUME);
        elementRacine.appendChild(elementResume);

        MAP_ANNOTATIONS_TROUVEES.forEach((annotation, nombre) -> {
            System.out.println(annotation + "=" + nombre + " phrases" );
            Element elementAnnotation = document.createElement(TAG_XML.ANNOTATION);
            elementAnnotation.setAttribute(ATTRIBUT_XML.NOM, annotation);
            elementAnnotation.setAttribute(ATTRIBUT_XML.NOMBRE_PHRASE, nombre.toString());
            elementResume.appendChild(elementAnnotation);
        });
    }

    private static void ajouterNouvellePhraseDansMapAnnotation(final String nomAnnotation) {
        Integer nombre = MAP_ANNOTATIONS_TROUVEES.get(nomAnnotation);
        if (nombre == null) {
            nombre = 1;
        } else {
            nombre = Integer.sum(nombre, 1);
        }
        MAP_ANNOTATIONS_TROUVEES.put(nomAnnotation, nombre);
    }

    private static void creerFichiersSorties(final Document document) throws DocletCucumberException {

        try {
            final TransformerFactory transformerFactory = TransformerFactory.newInstance();
            final Transformer transformerXML = transformerFactory.newTransformer();

            final boolean sortieHtml = outputToHtml;
            final boolean sortieTxt = outputToTxt;
            final boolean sortieXml = outputToXml;

            final DOMSource source = new DOMSource(document);

            String nomFichierSortie = name;
            if (Util.isNullOrEmpty(nomFichierSortie)) {
                nomFichierSortie = "JavadocCucumber";
            }
            String cheminComplet = fullPath;
            if (cheminComplet == null) {
                cheminComplet = "";
            }

            if (sortieXml || (!sortieHtml && !sortieTxt)) {
                Path pathFichierXml = Paths.get(cheminComplet, nomFichierSortie + ".xml" );
                final StreamResult sortieXML = new StreamResult(pathFichierXml.toFile());
                transformerXML.transform(source, sortieXML);
                System.out.println("Fichier '" + pathFichierXml + "' créé." );
            }

            if (sortieHtml) {
                String cheminCompletXslVersHtml = xslHtml;
                StreamSource stylesource;
                if (Util.isNullOrEmpty(cheminCompletXslVersHtml)) {
                    URL url = Cucumber.class.getClassLoader().getResource("doc/DocCucumberToHtml.xsl" );
                    if (url == null) {
                        throw new DocletCucumberException("impossible de trouver le fichier interne DocCucumberToHtml.xsl" );
                    }
                    stylesource = new StreamSource(url.openStream());
                } else {
                    stylesource = new StreamSource(new File(cheminCompletXslVersHtml));
                }

                final Transformer transformerHTML = transformerFactory.newTransformer(stylesource);
                final Path pathfichierHtml = Paths.get(cheminComplet, nomFichierSortie + ".html" );
                final StreamResult sortieHTML = new StreamResult(pathfichierHtml.toFile());
                transformerHTML.transform(source, sortieHTML);
                System.out.println("Fichier '" + pathfichierHtml + "' créé." );
            }
            if (sortieTxt) {
                StreamSource stylesource;
                String cheminCompletXslVersText = xslTxt;
                if (Util.isNullOrEmpty(cheminCompletXslVersText)) {
                    URL url = Cucumber.class.getClassLoader().getResource("doc/DocCucumberToTexte.xsl" );
                    if (url == null) {
                        throw new DocletCucumberException("impossible de trouver le fichier interne DocCucumberToTexte.xsl" );
                    }
                    stylesource = new StreamSource(url.openStream());
                } else {
                    stylesource = new StreamSource(new File(cheminCompletXslVersText));
                }

                final Transformer transformerTXT = transformerFactory.newTransformer(stylesource);
                final Path pathFichierTxt = Paths.get(cheminComplet, nomFichierSortie + ".txt" );
                final StreamResult sortieTXT = new StreamResult(pathFichierTxt.toFile());
                transformerTXT.transform(source, sortieTXT);
                System.out.println("Fichier '" + pathFichierTxt + "' créé." );
            }
        } catch (final TransformerException exe) {
            throw new DocletCucumberException("Erreur la préparation du fichier de sortie", exe);
        } catch (final IOException exe) {
            throw new DocletCucumberException("Erreur lors de la lecture du fichier de transformation xslt", exe);
        }
    }

    private static Element parseClass(final DocTrees docTrees, final Document document, final TypeElement typeElement) {
        Element elmClasse = document.createElement(TAG_XML.CLASSE);
        elmClasse.setAttribute(ATTRIBUT_XML.NOM, typeElement.getSimpleName().toString());

        typeElement.getEnclosedElements().stream()
                .filter(element -> element.getKind() == ElementKind.METHOD)
                .map(executableElement -> parseMethod(docTrees, document, (ExecutableElement) executableElement)) //
                .filter(Objects::nonNull) //
                .forEach(elmClasse::appendChild);

        return elmClasse.getChildNodes().getLength() > 0 ? elmClasse : null;
    }

    private static Element parseMethod(final DocTrees docTrees, final Document document, final ExecutableElement executableElement) {
        final Element elmMethode = document.createElement(TAG_XML.FONCTION);
        if (executableElement.getAnnotationMirrors() != null && executableElement.getAnnotationMirrors().size() > 0) {
            elmMethode.setAttribute(ATTRIBUT_XML.NOM, executableElement.getSimpleName().toString());

            executableElement.getAnnotationMirrors().stream()
                    .map(annotationMirror ->
                            parseAnnotation(document, elmMethode, annotationMirror, executableElement.getParameters()))
                    .filter(Objects::nonNull)
                    .forEach(elmMethode::appendChild);

            DocCommentTree commentTree = docTrees.getDocCommentTree(executableElement);
            if (elmMethode.getChildNodes().getLength() > 0) {
                parseParameter(document, elmMethode, executableElement.getParameters());
                DocCommentTree comment = docTrees.getDocCommentTree(executableElement);
                if (comment != null) {
                    parseComment(document, elmMethode, comment.toString());
                    List<ParamTree> paramTrees = commentTree.getBlockTags().stream()
                            .filter(param -> param.getKind() == DocTree.Kind.PARAM)
                            .map(param -> (ParamTree) param)
                            .collect(Collectors.toList());
                    parseParamTag(document, elmMethode, paramTrees);
                    parseTag(document, elmMethode, paramTrees.stream()
                                    .filter(paramTree -> paramTree.getTagName().equals(EXAMPLE))
                                            .collect(Collectors.toList()));
                }
                return elmMethode;
            }
        }

        return null;
    }

    private static void parseParameter(final Document document, final Element elmMethode, final List<? extends VariableElement> parameters) {
        for (VariableElement parameter : parameters) {
            Element elmTag = document.createElement(TAG_XML.PARAM);
            elmTag.setAttribute(ATTRIBUT_XML.NOM, parameter.getSimpleName().toString());
            elmTag.setAttribute(ATTRIBUT_XML.TYPE, parameter.getKind().name());
            elmMethode.appendChild(elmTag);
        }
    }

    private static Element parseComment(final Document document, final Element elmMethode, final String commentaire) {
        if (Util.isNotNullAndNotEmpty(commentaire)) {
            Element elmCommentaire = document.createElement(TAG_XML.COMMENTAIRE);
            String[] lignesCommentaire = SAUT_DE_LIGNE.split(commentaire);
            for (final String ligne : lignesCommentaire) {
                Element elmLigne = document.createElement(TAG_XML.LIGNE);
                elmLigne.setTextContent(ligne.replace("\"", "\\\"" ));
                elmCommentaire.appendChild(elmLigne);
            }
            elmMethode.appendChild(elmCommentaire);
        }

        return elmMethode;
    }

    private static void parseParamTag(final Document document, final Element elmMethode, final List<ParamTree> paramTrees) {
        for (ParamTree paramTree : paramTrees) {
            Element elmTag = document.createElement(TAG_XML.TAG);
            elmTag.setAttribute(ATTRIBUT_XML.NOM, paramTree.getTagName().replaceAll("@", "" ));
            elmTag.setAttribute(ATTRIBUT_XML.NOM_PARAMETRE, paramTree.getName().getName().toString());

            paramTree.getDescription().stream()
                    .filter(docTree -> docTree.getKind() == DocTree.Kind.COMMENT)
                    .map(Object::toString)
                    .forEach(comment -> {
                        Element elmLigne = document.createElement(TAG_XML.LIGNE);
                        elmLigne.setTextContent(comment);
                        elmTag.appendChild(elmLigne);
                    });
            elmMethode.appendChild(elmTag);
        }
    }

    private static void parseTag(final Document document, final Element elmMethode, final List<ParamTree> paramTrees) {
        for (final ParamTree paramTree : paramTrees) {
            Element elmTag = document.createElement(TAG_XML.TAG);
            elmTag.setAttribute(ATTRIBUT_XML.NOM, paramTree.getTagName().replace("@", "" ));

            String[] lignesCommentaireTag = SAUT_DE_LIGNE.split(paramTree.getDescription().stream()
                    .filter(docTree -> docTree.getKind() == DocTree.Kind.TEXT)
                    .map(Object::toString)
                    .collect(Collectors.joining()));
            for (final String ligne : lignesCommentaireTag) {
                Element elmLigne = document.createElement(TAG_XML.LIGNE);
                elmLigne.setTextContent(ligne);
                elmTag.appendChild(elmLigne);
            }
            elmMethode.appendChild(elmTag);
        }
    }

    private static Element parseAnnotation(final Document document, final Element elmFonction, final AnnotationMirror annotation, List<? extends VariableElement> parametres) {
        String nomAnnotation = Arrays.stream(annotation.getAnnotationType().toString().split("\\." ))
                .reduce((first, second) -> second)
                .orElse(null);

        if (!ANNOTATIONS_INCLUSES.contains(nomAnnotation)) {
            return null;
        }
        if (Objects.equals(nomAnnotation, DEPRECATED)) {
            elmFonction.setAttribute(ATTRIBUT_XML.DEPRECATED, "true" );
            ajouterNouvellePhraseDansMapAnnotation(DEPRECATED);
            return null;
        } else {
            final Element elmAnnotation = document.createElement(TAG_XML.ANNOTATION);
            elmAnnotation.setAttribute(ATTRIBUT_XML.NOM, nomAnnotation);
            annotation.getElementValues().entrySet().stream().map(Cucumber::docParContenuAnnotation).filter(Util::isNotNullAndNotEmpty)
                    .forEach(phrase -> {
                        phrase = phrase.replace("\"", "\\\"" );
                        elmAnnotation.setAttribute(ATTRIBUT_XML.PHRASE, phrase);
                        creerListeChoixPhrase(document, elmAnnotation, phrase, parametres);
                    });

            if (elmAnnotation.getAttribute(ATTRIBUT_XML.PHRASE) != null) {
                ajouterNouvellePhraseDansMapAnnotation(nomAnnotation);
                return elmAnnotation;
            }
            return null;
        }
    }

    private static void creerListeChoixPhrase(final Document document, final Element elmAnnotation, final String phrase, final List<? extends VariableElement> parametres) {
        List<String> listePhrasesPossibles = Util.extraireListePhrases(phrase);
        if (!listePhrasesPossibles.isEmpty()) {
            for (final String phrasePossible : listePhrasesPossibles) {
                Element elm = document.createElement(TAG_XML.PHRASE);
                elm.setTextContent(Util.ajoutParametreDansPhrasePossible(phrasePossible, parametres));
                elmAnnotation.appendChild(elm);
            }
        }
    }

    private static String docParContenuAnnotation(final Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry) {
        String phrase = null;
        AnnotationValue annotationValue = entry.getValue();
        if (annotationValue != null) {
            phrase = annotationValue.getValue().toString();
            phrase = phrase.replaceAll("[\\^$]", "" );
        }

        return phrase;
    }
}

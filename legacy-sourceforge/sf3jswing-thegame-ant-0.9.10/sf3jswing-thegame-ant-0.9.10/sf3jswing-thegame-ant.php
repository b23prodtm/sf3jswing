<?php
$bgcolortitle = "#0099ff";
$bgcolorplatform = "#99ccff";
?>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<HTML>
    <HEAD>
        <META HTTP-EQUIV="CONTENT-TYPE" CONTENT="text/html; charset=utf-8">
        <TITLE></TITLE>
        <META NAME="GENERATOR" CONTENT="OpenOffice.org 3.1  (Unix)">
        <META NAME="CREATED" CONTENT="20100102;6372900">
        <META NAME="CHANGED" CONTENT="20100102;6524600">
        <STYLE TYPE="text/css">
            <!--
            @page { margin: 2cm }
            P { margin-bottom: 0.21cm }
            TD P { margin-bottom: 0cm }
            A:link { so-language: zxx }
            -->
        </STYLE>
    </HEAD>
    <BODY LANG="fr-FR" DIR="LTR">
        <P ALIGN=CENTER STYLE="margin-bottom: 0cm"><BR>
        </P>
        <P ALIGN=CENTER STYLE="margin-bottom: 0cm"><BR>
        </P>
        <P ALIGN=CENTER STYLE="margin-bottom: 0cm"><BR>
        </P>
        <P ALIGN=CENTER STYLE="margin-bottom: 0cm"><BR>
        </P>
        <P ALIGN=CENTER STYLE="margin-bottom: 0cm"><BR>
        </P>
        <P ALIGN=CENTER STYLE="margin-bottom: 0cm"><BR>
        </P>
        <P ALIGN=CENTER STYLE="margin-bottom: 0cm"><BR>
        </P>
        <TABLE WIDTH=100% BORDER=3 BORDERCOLOR="#000080" CELLPADDING=0 CELLSPACING=0 FRAME=HSIDES RULES=ROWS>
            <COL WIDTH=128*>
            <COL WIDTH=128*>
            <TR VALIGN=BOTTOM>
                <TD WIDTH=50% BGCOLOR="<?php echo $bgcolortitle; ?>" SDNUM="1036;1036;@">
                    <P ALIGN=CENTER>
                        <?php $projectscreenoneurl = "@PROJECTSCREENONEURL@";
                        $projectscreensmalloneurl = "@PROJECTSCREENSMALLONEURL@";
                        $projectscreentwourl = "@PROJECTSCREENTWOURL@";
                        $projectscreensmalltwourl = "@PROJECTSCREENSMALLTWOURL@";
                        ?>
                        <img width="64" onclick="window.open('<?php echo $projectscreenoneurl;?>','','width=620,left=50,top=50,');" src="<?php echo $projectscreensmalloneurl; ?>">
                        <img width="64" onclick="window.open('<?php echo $projectscreentwourl;?>','','width=620,left=50,top=50,');" src="<?php echo $projectscreensmalltwourl; ?>">
                    </P>
                </TD>
                <TD WIDTH=50% BGCOLOR="<?php echo $bgcolortitle; ?>">
                    <P ALIGN=CENTER STYLE="margin-bottom: 0.5cm">
                        <?php
                        $title = "@PROJECTNAME@";
                        if ($_GET['title']) {
                            $title = urldecode($_GET['title']);
                        }
                        ?>
                    </P>
                    <P ALIGN=CENTER STYLE="text-decoration: none"><FONT COLOR="#ffffff"><FONT FACE="Andale Mono"><FONT SIZE=3><I><B>
<?php echo $title; ?>
                                        </B></I></FONT></FONT></FONT></P>
                </TD>
            </TR>
            <TR VALIGN=CENTER>
                <TD WIDTH=50% BGCOLOR="<?php echo $bgcolorplatform; ?>">
                    <P ALIGN=CENTER STYLE="margin-bottom: 0.5cm">
                    </P>
                    <P ALIGN=CENTER STYLE="font-style: normal; font-weight: normal; text-decoration: none">
                        <FONT COLOR="#000000"><FONT FACE="Andale Mono"><FONT SIZE=3>Choose
			your platform :</FONT></FONT></FONT></P>
                </TD>
                <TD WIDTH=50% BGCOLOR="<?php echo $bgcolorplatform; ?>">
                    <P ALIGN=CENTER STYLE="margin-bottom: 0.5cm">
                        <?php
                        $path = "@PROJECTCODEBASE@";
                        $filePath = "./";
                        $project = "@ANTPROJECTNAME@";

                        $releases = array("Windows" => "x86", "Mac" => "ppc", "Linux" => "i386");
                        if ($_GET["path"])
                            $path = urldecode($_GET['path']);
                        if ($_GET["filePath"]) {
                            $filePath = urldecode($_GET['filePath']);
                        }
                        if ($_GET['project'])
                            $project = urldecode($_GET['project']);
                        if (is_array($_GET['releases']))
                            $release = urldecode($_GET['releases']);

                        function manifestVersion($filename) {
                            $lookFor = "Implementation-Version: ";
                            $ret = "n/a";
                            $zipArch = new ZipArchive();
                            if ($zipArch->open($filename) === true) {
                                $manifestData = $zipArch->getFromName("META-INF/MANIFEST.MF");
                                if ($manifestData !== false) {
                                    $manifestArr = split("[\n\r]+", $manifestData);
                                    foreach ($manifestArr as $tok) {
                                        if (substr($tok, 0, strlen($lookFor)) === $lookFor) {
                                            $ret = substr($tok, strlen($lookFor), 8);
                                            break;
                                        }
                                    }
                                }
                            }
                            if ($ret === "n/a" || $ret === false) {
                                echo $zipArch->getStatusString();
                            }
                            $zipArch->close();
                            return $ret;
                        }

                        function printReleases($filePath, $path, $project, $releases) {
                            $files;
                            $i = 0;
                            echo "<table border=0 width=\"100%\"><tr>";
                            foreach ($releases as $relOs => $relArch) {
                                $files[$i++] = $filePath . $project . "-" . $relOs . "-" . $relArch . "_signed.jar";
                                echo "<td><A HREF=\"" . $path . $project . "-" . $relOs . "-" . $relArch . ".html\">" . $relOs . "</A>\r\n" .
                                "</td>";
                            }
                            echo"</tr><tr>";
                            foreach ($files as $filename) {
                                echo "<td>" . manifestVersion($filename) . "\r\n" .
                                "</td>";
                            }
                            echo "</tr></table>";
                        }
                        ?>
                    </P> <P ALIGN=CENTER STYLE="margin-bottom: 0.5cm"><FONT COLOR="#000000"><SPAN STYLE="text-decoration: none"><FONT FACE="Andale Mono"><FONT SIZE=3><SPAN STYLE="font-style: normal"><SPAN STYLE="font-weight: normal">
<?php printReleases($filePath, $path, $project, $releases); ?>
                                            </SPAN></SPAN></FONT></FONT></SPAN></FONT></P>
                    <P ALIGN=CENTER><BR>
                    </P>
                </TD>
            </TR>
            <TR>
                <TD COLSPAN=2 WIDTH=100% VALIGN=TOP BGCOLOR="#ffffff">
                    <p><?php
                        $companyname = "@COMPANYNAME@";
                        $companyurl = "@COMPANYURL@";
                        $projectyears = "@PROJECTYEARS@";
                        $projectname = "@PROJECTNAME@";
                        $companyemail = "@COMPANYEMAIL@";
?></p>
                    <P ALIGN=CENTER><FONT COLOR="#000000"><SPAN STYLE="text-decoration: none"><FONT FACE="Arial, sans-serif"><FONT SIZE=3><SPAN STYLE="font-style: normal"><SPAN STYLE="font-weight: normal">&copy;</SPAN></SPAN></FONT></FONT></SPAN></FONT><FONT COLOR="#000000"><SPAN STYLE="text-decoration: none"><FONT FACE="Thorndale, serif"><FONT SIZE=3><SPAN STYLE="font-style: normal"><SPAN STYLE="font-weight: normal">
<?php echo $projectyears; ?> - <A HREF="<?php echo $companyurl; ?>"><?php echo $companyname; ?></A>
			 - <A HREF="<?php echo $projecturl; ?>"><?php echo $projectname; ?></A> - <A HREF="mailto:<?php echo $companyemail; ?>">Contact Mail</A>
                                            </SPAN></SPAN></FONT></FONT></SPAN></FONT></P>
                </TD>
            </TR>
        </TABLE>
        <P ALIGN=CENTER STYLE="margin-bottom: 0cm"><BR>
        </P>
        <P ALIGN=CENTER STYLE="margin-bottom: 0cm"><BR>
        </P>
        <P ALIGN=CENTER STYLE="margin-bottom: 0cm"><BR>
        </P>
        <P ALIGN=CENTER STYLE="margin-bottom: 0cm"><BR>
        </P>
        <P ALIGN=CENTER STYLE="margin-bottom: 0cm"><BR>
        </P>
        <P ALIGN=CENTER STYLE="margin-bottom: 0cm"><BR>
        </P>
        <P ALIGN=CENTER STYLE="margin-bottom: 0cm"><BR>
        </P>
    </BODY>
</HTML>

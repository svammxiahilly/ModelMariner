import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:material_symbols_icons/material_symbols_icons.dart';
import '../utils/open_links.dart';
import 'package:flutter_markdown/flutter_markdown.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';

class AboutScreen extends StatelessWidget {
  const AboutScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final colorTheme = Theme.of(context).colorScheme;
    final appIcon =
        ''' <svg width="221" height="222" viewBox="0 0 221 222" fill="none" xmlns="http://www.w3.org/2000/svg">
<circle cx="110.5" cy="110.528" r="110.5" fill="#F3F1E8"/>
<path d="M111 111.028L55 188.028" stroke="#2B1F0D" stroke-width="2" stroke-linecap="round"/>
<path d="M188 55.0278L112 110.028" stroke="#32351E" stroke-width="5" stroke-linecap="round"/>
<path d="M63 78.0278L112 114.028" stroke="#4C4400" stroke-width="16" stroke-linecap="round"/>
</svg>
 ''';
    return Scaffold(
      appBar: AppBar(
        automaticallyImplyLeading: false,
        backgroundColor: Theme.of(context).colorScheme.surfaceContainer,
        elevation: 0,
        toolbarHeight: 120,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.only(
            bottomLeft: Radius.circular(28),
            bottomRight: Radius.circular(28),
          ),
        ),
        title: Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Column(
              spacing: 10,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  "ClockMaster",
                  style: TextStyle(
                    color: Theme.of(context).colorScheme.secondary,
                    fontSize: 24,
                  ),
                ),
                CheckUpdateButton(),
              ],
            ),
            Container(
              clipBehavior: Clip.hardEdge,
              decoration: BoxDecoration(
                borderRadius: BorderRadius.circular(50),
              ),
              child: SvgPicture.string(appIcon, width: 80, height: 80),
            ),
          ],
        ),
      ),
      body: ListView(
        children: [
          ListTile(
            leading: CircleAvatar(radius: 23, child: Icon(Symbols.license)),
            title: Text("Licenses"),
            subtitle: Text("GNU GPL-3.0"),
          ),
          ListTile(
            leading: CircleAvatar(radius: 23, child: Icon(Symbols.mail)),
            title: Text("Email"),
            subtitle: Text("pranshul.devmain@gmail.com"),
            onTap: () {
              openLink("mailto:pranshul.devmain@gmail.com");
            },
          ),
          ListTile(
            leading: CircleAvatar(radius: 23, child: Icon(Symbols.code)),
            title: Text("Source code"),
            subtitle: Text("On GitHub"),
            onTap: () {
              openLink("https://github.com/PranshulGG/ClockMaster");
            },
          ),
          ListTile(
            leading: CircleAvatar(radius: 23, child: Icon(Symbols.bug_report)),
            title: Text("Create an issue"),
            subtitle: Text("On GitHub"),
            onTap: () {
              openLink("https://github.com/PranshulGG/ClockMaster/issues/");
            },
          ),
        ],
      ),
      bottomNavigationBar: ClipRRect(
        borderRadius: BorderRadius.only(
          topLeft: Radius.circular(28),
          topRight: Radius.circular(28),
        ),
        child: BottomAppBar(
          elevation: 0,
          height: 180,
          padding: EdgeInsets.only(top: 10),
          color: Theme.of(context).colorScheme.surfaceContainer,
          child: ListView(
            physics: NeverScrollableScrollPhysics(),
            children: [
              ListTile(
                leading: CircleAvatar(radius: 23, child: Icon(Symbols.license)),
                title: Text("Third party licenses"),
                onTap: () {
                  showLicensePage(
                    context: context,
                    applicationName: 'ClockMaster',
                    applicationVersion: 'v1.4.0',
                    applicationIcon: Container(
                      clipBehavior: Clip.hardEdge,
                      margin: EdgeInsets.only(bottom: 16, top: 16),
                      decoration: BoxDecoration(
                        borderRadius: BorderRadius.circular(50),
                      ),
                      child: SvgPicture.string(appIcon, width: 60, height: 60),
                    ),
                  );
                },
              ),
              ListTile(
                leading: CircleAvatar(radius: 23, child: Icon(Symbols.mail)),
                title: Text("Terms & conditions"),
                onTap: () {
                  Navigator.of(context).push(
                    PageRouteBuilder(
                      opaque: true,
                      fullscreenDialog: true,
                      reverseTransitionDuration: Duration(milliseconds: 200),
                      pageBuilder: (context, animation, secondaryAnimation) {
                        return TermsPage();
                      },
                      transitionsBuilder:
                          (context, animation, secondaryAnimation, child) {
                            return FadeTransition(
                              opacity: animation,
                              child: child,
                            );
                          },
                    ),
                  );
                },
              ),
              ListTile(
                leading: CircleAvatar(radius: 23, child: Icon(Symbols.code)),
                title: Text("Privacy policy"),
                onTap: () {
                  Navigator.of(context).push(
                    PageRouteBuilder(
                      opaque: true,
                      fullscreenDialog: true,
                      reverseTransitionDuration: Duration(milliseconds: 200),
                      pageBuilder: (context, animation, secondaryAnimation) {
                        return PolicyPage();
                      },
                      transitionsBuilder:
                          (context, animation, secondaryAnimation, child) {
                            return FadeTransition(
                              opacity: animation,
                              child: child,
                            );
                          },
                    ),
                  );
                },
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class TermsPage extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    final markdownData = '''
**Terms & Conditions**  

These terms and conditions apply to the ClockMaster app (hereby referred to as "Application") for mobile devices that was created by Pranshul (hereby referred to as "Service Provider") as an Open Source service.

Upon downloading or utilizing the Application, you are automatically agreeing to the following terms. It is strongly advised that you thoroughly read and understand these terms prior to using the Application. Unauthorized copying, modification of the Application, any part of the Application, or our trademarks is strictly prohibited. Any attempts to extract the source code of the Application, translate the Application into other languages, or create derivative versions are not permitted. All trademarks, copyrights, database rights, and other intellectual property rights related to the Application remain the property of the Service Provider.

The Service Provider is dedicated to ensuring that the Application is as beneficial and efficient as possible. As such, they reserve the right to modify the Application or charge for their services at any time and for any reason. The Service Provider assures you that any charges for the Application or its services will be clearly communicated to you.

The Application stores and processes personal data that you have provided to the Service Provider in order to provide the Service. It is your responsibility to maintain the security of your phone and access to the Application. The Service Provider strongly advise against jailbreaking or rooting your phone, which involves removing software restrictions and limitations imposed by the official operating system of your device. Such actions could expose your phone to malware, viruses, malicious programs, compromise your phone's security features, and may result in the Application not functioning correctly or at all.

Please be aware that the Service Provider does not assume responsibility for certain aspects. Some functions of the Application require an active internet connection, which can be Wi-Fi or provided by your mobile network provider. The Service Provider cannot be held responsible if the Application does not function at full capacity due to lack of access to Wi-Fi or if you have exhausted your data allowance.

If you are using the application outside of a Wi-Fi area, please be aware that your mobile network provider's agreement terms still apply. Consequently, you may incur charges from your mobile provider for data usage during the connection to the application, or other third-party charges. By using the application, you accept responsibility for any such charges, including roaming data charges if you use the application outside of your home territory (i.e., region or country) without disabling data roaming. If you are not the bill payer for the device on which you are using the application, they assume that you have obtained permission from the bill payer.

Similarly, the Service Provider cannot always assume responsibility for your usage of the application. For instance, it is your responsibility to ensure that your device remains charged. If your device runs out of battery and you are unable to access the Service, the Service Provider cannot be held responsible.

In terms of the Service Provider's responsibility for your use of the application, it is important to note that while they strive to ensure that it is updated and accurate at all times, they do rely on third parties to provide information to them so that they can make it available to you. The Service Provider accepts no liability for any loss, direct or indirect, that you experience as a result of relying entirely on this functionality of the application.

The Service Provider may wish to update the application at some point. The application is currently available as per the requirements for the operating system (and for any additional systems they decide to extend the availability of the application to) may change, and you will need to download the updates if you want to continue using the application. The Service Provider does not guarantee that it will always update the application so that it is relevant to you and/or compatible with the particular operating system version installed on your device. However, you agree to always accept updates to the application when offered to you. The Service Provider may also wish to cease providing the application and may terminate its use at any time without providing termination notice to you. Unless they inform you otherwise, upon any termination, (a) the rights and licenses granted to you in these terms will end; (b) you must cease using the application, and (if necessary) delete it from your device.

**Changes to These Terms and Conditions**

The Service Provider may periodically update their Terms and Conditions. Therefore, you are advised to review this page regularly for any changes. The Service Provider will notify you of any changes by posting the new Terms and Conditions on this page.

These terms and conditions are effective as of 2025-08-27

**Contact Us**

If you have any questions or suggestions about the Terms and Conditions, please do not hesitate to contact the Service Provider at pranshul.devmain@gmail.com.

* * *

This Terms and Conditions page was generated by [App Privacy Policy Generator](https://app-privacy-policy-generator.nisrulz.com/)
''';

    return Scaffold(
      appBar: AppBar(
        title: Text('Terms & conditions'),
        titleSpacing: 0,
        scrolledUnderElevation: 0,
      ),
      body: Markdown(
        data: markdownData,
        padding: EdgeInsets.only(
          top: 16,
          bottom: MediaQuery.of(context).padding.bottom + 10,
          left: 16,
          right: 16,
        ),
        onTapLink: (text, href, title) async {
          openLink(href.toString());
        },
      ),
    );
  }
}

class PolicyPage extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    final markdownData = '''
**Privacy Policy**

This privacy policy applies to the ClockMaster app (hereby referred to as "Application") for mobile devices that was created by Pranshul (hereby referred to as "Service Provider") as an Open Source service. This service is intended for use "AS IS".

**Information Collection and Use**

The Application collects information when you download and use it. This information may include information such as

*   Your device's Internet Protocol address (e.g. IP address)
*   The pages of the Application that you visit, the time and date of your visit, the time spent on those pages
*   The time spent on the Application
*   The operating system you use on your mobile device

The Application does not gather precise information about the location of your mobile device.

The Application collects your device's location, which helps the Service Provider determine your approximate geographical location and make use of in below ways:

*   Geolocation Services: The Service Provider utilizes location data to provide features such as personalized content, relevant recommendations, and location-based services.
*   Analytics and Improvements: Aggregated and anonymized location data helps the Service Provider to analyze user behavior, identify trends, and improve the overall performance and functionality of the Application.
*   Third-Party Services: Periodically, the Service Provider may transmit anonymized location data to external services. These services assist them in enhancing the Application and optimizing their offerings.

The Service Provider may use the information you provided to contact you from time to time to provide you with important information, required notices and marketing promotions.

For a better experience, while using the Application, the Service Provider may require you to provide us with certain personally identifiable information. The information that the Service Provider request will be retained by them and used as described in this privacy policy.

**Third Party Access**

Only aggregated, anonymized data is periodically transmitted to external services to aid the Service Provider in improving the Application and their service. The Service Provider may share your information with third parties in the ways that are described in this privacy statement.

The Service Provider may disclose User Provided and Automatically Collected Information:

*   as required by law, such as to comply with a subpoena, or similar legal process;
*   when they believe in good faith that disclosure is necessary to protect their rights, protect your safety or the safety of others, investigate fraud, or respond to a government request;
*   with their trusted services providers who work on their behalf, do not have an independent use of the information we disclose to them, and have agreed to adhere to the rules set forth in this privacy statement.

**Opt-Out Rights**

You can stop all collection of information by the Application easily by uninstalling it. You may use the standard uninstall processes as may be available as part of your mobile device or via the mobile application marketplace or network.

**Data Retention Policy**

The Service Provider will retain User Provided data for as long as you use the Application and for a reasonable time thereafter. If you'd like them to delete User Provided Data that you have provided via the Application, please contact them at pranshul.devmain@gmail.com and they will respond in a reasonable time.

**Children**

The Service Provider does not use the Application to knowingly solicit data from or market to children under the age of 13.

The Service Provider does not knowingly collect personally identifiable information from children. The Service Provider encourages all children to never submit any personally identifiable information through the Application and/or Services. The Service Provider encourage parents and legal guardians to monitor their children's Internet usage and to help enforce this Policy by instructing their children never to provide personally identifiable information through the Application and/or Services without their permission. If you have reason to believe that a child has provided personally identifiable information to the Service Provider through the Application and/or Services, please contact the Service Provider (pranshul.devmain@gmail.com) so that they will be able to take the necessary actions. You must also be at least 16 years of age to consent to the processing of your personally identifiable information in your country (in some countries we may allow your parent or guardian to do so on your behalf).

**Security**

The Service Provider is concerned about safeguarding the confidentiality of your information. The Service Provider provides physical, electronic, and procedural safeguards to protect information the Service Provider processes and maintains.

**Changes**

This Privacy Policy may be updated from time to time for any reason. The Service Provider will notify you of any changes to the Privacy Policy by updating this page with the new Privacy Policy. You are advised to consult this Privacy Policy regularly for any changes, as continued use is deemed approval of all changes.

This privacy policy is effective as of 2025-08-27

**Your Consent**

By using the Application, you are consenting to the processing of your information as set forth in this Privacy Policy now and as amended by us.

**Contact Us**

If you have any questions regarding privacy while using the Application, or have questions about the practices, please contact the Service Provider via email at pranshul.devmain@gmail.com.

* * *

This privacy policy page was generated by [App Privacy Policy Generator](https://app-privacy-policy-generator.nisrulz.com/)
''';

    return Scaffold(
      appBar: AppBar(
        title: Text('Privacy policy'),
        titleSpacing: 0,
        scrolledUnderElevation: 0,
      ),
      body: Markdown(
        data: markdownData,
        padding: EdgeInsets.only(
          top: 16,
          bottom: MediaQuery.of(context).padding.bottom + 10,
          left: 16,
          right: 16,
        ),
        onTapLink: (text, href, title) async {
          openLink(href.toString());
        },
      ),
    );
  }
}

class CheckUpdateButton extends StatefulWidget {
  @override
  _CheckUpdateButtonState createState() => _CheckUpdateButtonState();
}

class _CheckUpdateButtonState extends State<CheckUpdateButton> {
  final String currentVersion = 'v1.4.0';
  final String githubRepo = 'PranshulGG/ClockMaster';
  bool isChecking = false;

  Future<void> checkForUpdates() async {
    setState(() {
      isChecking = true;
    });

    final String releasesUrl =
        'https://api.github.com/repos/$githubRepo/releases';

    try {
      final response = await http.get(Uri.parse(releasesUrl));
      if (response.statusCode != 200) {
        throw Exception('Failed to fetch releases');
      }

      final List<dynamic> releases = jsonDecode(response.body);
      final latestStable = releases.firstWhere(
        (release) => release['prerelease'] == false,
        orElse: () => null,
      );

      await Future.delayed(Duration(seconds: 2));

      if (latestStable != null && latestStable['tag_name'] != currentVersion) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('New version available!'),
            behavior: SnackBarBehavior.floating,
          ),
        );

        await Future.delayed(Duration(seconds: 1));

        final url = 'https://github.com/$githubRepo/releases';
        openLink(url);
      } else {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('You are using the latest version!'),
            behavior: SnackBarBehavior.floating,
          ),
        );
      }
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('Error checking for updates'),
          behavior: SnackBarBehavior.floating,
        ),
      );
      print('Error: $e');
    }

    setState(() {
      isChecking = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    return FilledButton.icon(
      onPressed: isChecking ? null : checkForUpdates,
      icon: Icon(Symbols.refresh, weight: 700),
      label: Text(
        isChecking ? 'Checking' : currentVersion,
        style: TextStyle(fontWeight: FontWeight.w700),
      ),
      style: ButtonStyle(
        padding: WidgetStateProperty.all(
          EdgeInsets.symmetric(horizontal: 12, vertical: 5),
        ),
        minimumSize: WidgetStateProperty.all(Size(0, 30)),
        iconAlignment: IconAlignment.end,
        tapTargetSize: MaterialTapTargetSize.shrinkWrap,
      ),
    );
  }
}

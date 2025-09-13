import 'package:easy_localization/easy_localization.dart';
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
            title: Text("licenses".tr()),
            subtitle: Text("GNU GPL-3.0"),
          ),
          ListTile(
            leading: CircleAvatar(radius: 23, child: Icon(Symbols.mail)),
            title: Text("email".tr()),
            subtitle: Text("pranshul.devmain@gmail.com"),
            onTap: () {
              openLink("mailto:pranshul.devmain@gmail.com");
            },
          ),
          ListTile(
            leading: CircleAvatar(radius: 23, child: Icon(Symbols.code)),
            title: Text("source_code".tr()),
            subtitle: Text("on_github".tr()),
            onTap: () {
              openLink("https://github.com/PranshulGG/ClockMaster");
            },
          ),
          ListTile(
            leading: CircleAvatar(radius: 23, child: Icon(Symbols.bug_report)),
            title: Text("create_an_issue".tr()),
            subtitle: Text("on_github".tr()),
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
                title: Text("third_party_licenses".tr()),
                onTap: () {
                  Navigator.of(context).push(
                    PageRouteBuilder(
                      reverseTransitionDuration: Duration(milliseconds: 200),
                      pageBuilder: (context, animation, secondaryAnimation) {
                        return LicensePage(
                          applicationName: 'ClockMaster',
                          applicationVersion: 'v1.4.3',
                          applicationIcon: Container(
                            clipBehavior: Clip.hardEdge,
                            margin: EdgeInsets.only(bottom: 16, top: 16),
                            decoration: BoxDecoration(
                              borderRadius: BorderRadius.circular(50),
                            ),
                            child: SvgPicture.string(
                              appIcon,
                              width: 60,
                              height: 60,
                            ),
                          ),
                        );
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
                leading: CircleAvatar(radius: 23, child: Icon(Symbols.mail)),
                title: Text("terms_and_conditions".tr()),
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
                title: Text("privacy_policy".tr()),
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

These Terms & Conditions apply to the **ClockMaster** app (the "Application") for mobile devices.  
This app was created by **Pranshul** as an open-source, hobby project. By using the Application, you agree to the following:  

## Use of the Application  
- The Application is provided **as-is**, free of charge, and without any guarantees of reliability, availability, or accuracy.  
- You may use, modify, and distribute the Application in accordance with its open-source license.  
- You may **not** misrepresent the origin of the Application or use its name/trademarks without permission.  

## Data & Privacy  
- The Application does **not collect, store, or share** any personal information.  
- The Application may request permission to send **notifications**, which are handled entirely on your device.  
- For more details, please see the Privacy Policy.  

## Liability  
- The Service Provider (Pranshul) is **not liable** for any direct or indirect damages, losses, or issues that may arise from using the Application.  
- This includes (but is not limited to) inaccurate information, device issues, or mobile data charges.  
- You are responsible for ensuring your device is compatible and has sufficient internet and battery to use the Application.  

## Updates & Availability  
- The Application may be updated from time to time.  
- There is no guarantee that the Application will always remain available, functional, or supported on all operating system versions.  
- The Service Provider may discontinue the Application at any time without prior notice.  

## Changes to These Terms  
These Terms & Conditions may be updated in the future. Updates will be posted in the project repository or within the Application. Continued use of the Application means you accept any revised terms.  

## Contact  
If you have any questions about these Terms & Conditions, please contact:  
📧 **pranshul.devmain@gmail.com**  




''';

    return Scaffold(
      appBar: AppBar(
        title: Text('terms_and_conditions'.tr()),
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

ClockMaster is an open-source application.  

We respect your privacy. This application:  

- Does **not collect, store, or share** any personal information.  
- Does **not track** your IP address, usage data, or any identifiers.  
- Does **not send data** to us or to third parties.  

## Notifications  
The app may request permission to send you notifications.  
- Granting notification permission is **Required on Android 12+**.  
- Notifications are generated and handled **locally on your device**.  
- No notification data is collected, stored, or shared.  

## Children  
This application does not knowingly collect any personal information from anyone, including children under 13.  

## Changes  
If this Privacy Policy changes, we will update it here. Continued use of the app after changes means you accept the revised policy.  

## Contact  
If you have any questions about privacy while using ClockMaster, please contact us at:  
📧 **pranshul.devmain@gmail.com**  

''';

    return Scaffold(
      appBar: AppBar(
        title: Text('privacy_policy'.tr()),
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
  final String currentVersion = 'v1.4.3';
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

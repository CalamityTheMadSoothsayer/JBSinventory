# JBSinventory
Android app for inventory taking porocess @ JBS in conjunction with a file explorer like X-plore

Made to simplify the inventory application process at JBS. Increased efficiency 30-40%.

---------------------------------------------------------------------------------------------
INITIAL INSTALLATION:

Open the app.

Click the 'Update' button located in the top left corner.
Click the 3 download buttons (Inv, Projects, Chemicals)

Close the app completely and restart.

The application is now ready to use.
---------------------------------------------------------------------------------------------


---------------------------------------------------------------------------------------------
Downloaded files notes:

Located it in 'Android/data/com.rockwell.jbsinv/files'

InventoryJBS.xls:
This is your blank spreadsheet. It will be copied upon opening the app. The new spreadshett
will be named after the date (i.e. 20220421.xls). Once you've completed the inventory
taking process, the copied file is the one you will turn in.

products.txt:
Contains all the product numbers located in the spreadsheet. This file controls the cycle
order within the app. 

This means if 600-123456 is the first number in products.txt, this is the number that will
show up first in the app. Clicking the forward button will go to the next number in
products.txt and so on.

chemicals.txt:
Same as products.txt but contains all chemical product numbers.
---------------------------------------------------------------------------------------------


---------------------------------------------------------------------------------------------
General use:

Entering totals:
You can either enter the total directly (i.e. '20' for 120 cases) or enter a math function
to let the app calculate the total for you (i.e. 7*16+8 will also give you 120 cases) once
you hit the update count button.

Warehouse inventory tesxtbox: 
Here you will enter the number of FULL bag cases or FULL pallets of cardboard.

Partials:
Here you will enter the number of OPEN bag cases or PARTIAL but SEALED cardboard pallets.

Trayformer / Pink:
Here you will enter the number of FULL cardboard located at the trayformer or the total
located on the PINK stickers on SEALED bag cases.

Bookmark:
This button will save the current product number you are on. You can then use the search
to deal with another product while keeping your place during the process.

Recall:
This button will return you to your saved position saved by the bookmark button.

Search:
You can manually type in a product number that may be out of place in the warehouse
and click the Search button to bring it up in the app.

If you have incorrectly entered a product, or an incorrect number has been entered
in either text file, every button besides this one will be locked. You will need to
correctly enter a product number and search it to continue.

If the issue is in one of the text files, have it corrected and re-uploaded to the
server.

If the issue is manually entered. Either correct the entry or use Recall if
bookmarked.

Back/Forw:
Use these button to cycle through the inventory in the order defined by the text files.
---------------------------------------------------------------------------------------------


import * as React from 'react';
import {Outlet} from "react-router-dom";


function MainContent() {
    return(
        <main className="main-content">
            <Outlet/>
        </main>
    )
}
export default MainContent
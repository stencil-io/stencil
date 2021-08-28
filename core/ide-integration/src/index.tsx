import React from 'react';
import ReactDOM from 'react-dom';

import { ThemeProvider } from '@material-ui/core';
import { IntlProvider } from 'react-intl'

import { CMSEditor, API, messages } from '@the-stencil-io/composer';
import { siteTheme } from './themes/siteTheme'

const locale = "en";

declare global {
  interface Window {
    portalconfig?: { server: { url: string } },
  }
}

const { portalconfig } = window;

ReactDOM.render(
  <IntlProvider locale={locale} messages={messages[locale]}>
    <ThemeProvider theme={siteTheme}>
      <CMSEditor service={API.service({url: portalconfig?.server.url ? portalconfig.server.url : ""})} />
    </ThemeProvider>
  </IntlProvider>
  ,
  document.getElementById('root')
);
import { Box, Container, Link, Typography } from '@mui/material';
import about from '../../content/about.json';
import { TermsText } from '../auth/TermsDialog';

interface Block { tag: string; text: string; links: { text: string; href: string }[] }

/** purpose.zul + purposeContent.zul (static). External links preserved; the empty "Terms of Use" link now shows the terms. */
export function AboutPage() {
  const blocks = about as Block[];
  const listItems = blocks.filter((b) => b.tag === 'li');
  return (
    <Container maxWidth="md" sx={{ py: 5, lineHeight: 1.8 }}>
      {blocks.map((b, i) => {
        if (b.tag === 'h2')
          return <Typography key={i} variant="h4" component="h1" gutterBottom>{b.text}</Typography>;
        if (b.tag.startsWith('h'))
          return <Typography key={i} variant="h5" component="h2" sx={{ mt: 4, mb: 1 }}>{b.text}</Typography>;
        if (b.tag === 'li') {
          if (b !== listItems[0]) return null;
          return (
            <Box component="ul" key={i} sx={{ pl: 3 }}>
              {listItems.map((li, j) => <li key={j}><Typography sx={{ fontSize: 17 }}>{li.text}</Typography></li>)}
            </Box>
          );
        }
        return (
          <Typography key={i} paragraph sx={{ fontSize: 17, lineHeight: 1.8 }}>
            {linkify(b.text, b.links)}
          </Typography>
        );
      })}
      <Typography paragraph sx={{ fontSize: 17, lineHeight: 1.8 }}>
        The SIBOL-EVES database builds upon the foundation established by these existing databases to provide a specific focus on empirical studies related
        to ecosystem service valuation in the Philippines and comparable international sites in Southeast Asia. It enhances the availability of information
        and contributes to the broader understanding of ecosystem services and their economic value.
      </Typography>
      <Typography paragraph sx={{ fontSize: 17, lineHeight: 1.8 }}>
        The UNEP 2013 Guidance Manual on Value Transfer Methods for Ecosystem Services offers valuable guidance for using estimated economic values of
        ecosystem services in benefits transfer. It provides comprehensive information on how to transfer values between studies or locations accurately,
        ensuring reliable ecosystem services valuation. Access the manual at this{' '}
        <Link href="https://www.unep.org/resources/report/guidance-manual-value-transfer-methods-ecosystem-services" target="_blank" rel="noopener">
          link
        </Link>
        .
      </Typography>
      <Typography variant="h5" component="h2" sx={{ mt: 4, mb: 1 }} id="terms">Terms of Use</Typography>
      <TermsText />
    </Container>
  );
}

function linkify(text: string, links: { text: string; href: string }[]) {
  if (!links.length) return text;
  const parts: (string | JSX.Element)[] = [];
  let rest = text;
  links.forEach((l, i) => {
    const idx = rest.indexOf(l.text);
    if (idx < 0) return;
    parts.push(rest.slice(0, idx));
    parts.push(<Link key={i} href={l.href} target="_blank" rel="noopener">{l.text}</Link>);
    rest = rest.slice(idx + l.text.length);
  });
  parts.push(rest);
  return parts;
}

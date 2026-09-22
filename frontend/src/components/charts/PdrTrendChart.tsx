import React, { useState } from 'react';

export interface DataPoint {
  mois: string;
  quantite: number;
  isPrediction?: boolean;
}

interface PdrTrendChartProps {
  historique?: DataPoint[];
  seuil?: number;
  previsionM1?: number | null;
  quantiteInstallee?: number;
}

export const PdrTrendChart: React.FC<PdrTrendChartProps> = ({
  historique = [],
  seuil = 0,
  previsionM1 = null,
  quantiteInstallee = 0,
}) => {
  const [hoveredPoint, setHoveredPoint] = useState<DataPoint | null>(null);

  const dataPoints: DataPoint[] = [...historique];
  if (previsionM1 !== null && previsionM1 !== undefined) {
    dataPoints.push({
      mois: 'M+1 (Prévu)',
      quantite: previsionM1,
      isPrediction: true,
    });
  }

  if (dataPoints.length === 0) {
    return (
      <div style={{ padding: '24px', textAlign: 'center', color: 'var(--ocp-text-muted)', fontSize: '14px' }}>
        <span>Aucun historique disponible pour tracer le graphique.</span>
      </div>
    );
  }

  const width = 640;
  const height = 240;
  const padding = { top: 30, right: 35, bottom: 45, left: 55 };

  const chartWidth = width - padding.left - padding.right;
  const chartHeight = height - padding.top - padding.bottom;

  const values = dataPoints.map((d) => d.quantite);
  if (seuil > 0) values.push(seuil);
  const maxVal = Math.max(...values, 1) * 1.15;
  const minVal = 0;

  const getX = (index: number) => {
    if (dataPoints.length === 1) return padding.left + chartWidth / 2;
    return padding.left + (index / (dataPoints.length - 1)) * chartWidth;
  };

  const getY = (val: number) => {
    return padding.top + chartHeight - ((val - minVal) / (maxVal - minVal)) * chartHeight;
  };

  const seuilY = seuil > 0 ? getY(seuil) : null;

  const historyCoords = dataPoints
    .filter((d) => !d.isPrediction)
    .map((d, i) => ({ x: getX(i), y: getY(d.quantite), d }));

  const historyPath = historyCoords.length > 0
    ? historyCoords.reduce((acc, pt, i) => `${acc} ${i === 0 ? 'M' : 'L'} ${pt.x},${pt.y}`, '')
    : '';

  const areaPath = historyCoords.length > 1
    ? `${historyPath} L ${historyCoords[historyCoords.length - 1].x},${padding.top + chartHeight} L ${historyCoords[0].x},${padding.top + chartHeight} Z`
    : '';

  let predictionLinkPath = '';
  let predCoord: { x: number; y: number } | null = null;
  if (previsionM1 !== null && previsionM1 !== undefined && historyCoords.length > 0) {
    const lastHist = historyCoords[historyCoords.length - 1];
    predCoord = { x: getX(dataPoints.length - 1), y: getY(previsionM1) };
    predictionLinkPath = `M ${lastHist.x},${lastHist.y} L ${predCoord.x},${predCoord.y}`;
  }

  const yTicks = [0, maxVal * 0.33, maxVal * 0.66, maxVal];

  return (
    <div style={{ background: 'var(--ocp-card-bg)', borderRadius: '12px', border: '1px solid var(--ocp-border)', padding: '16px' }}>
      <div style={{ display: 'flex', gap: '16px', alignItems: 'center', marginBottom: '12px', fontSize: '13px', flexWrap: 'wrap' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
          <span style={{ width: '12px', height: '12px', borderRadius: '50%', background: '#3b82f6' }} />
          <span style={{ color: 'var(--ocp-text-muted)', fontWeight: 500 }}>Historique Réel</span>
        </div>
        {seuil > 0 && (
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
            <span style={{ width: '16px', height: '2px', background: '#ef4444', borderTop: '2px dashed #ef4444' }} />
            <span style={{ color: '#ef4444', fontWeight: 500 }}>Seuil OCP 200% ({seuil.toFixed(1)})</span>
          </div>
        )}
        {previsionM1 !== null && (
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
            <span style={{ width: '12px', height: '12px', borderRadius: '50%', background: '#8b5cf6' }} />
            <span style={{ color: '#8b5cf6', fontWeight: 600 }}>Prédiction IA ({previsionM1.toFixed(1)})</span>
          </div>
        )}
        {quantiteInstallee > 0 && (
          <span style={{ marginLeft: 'auto', color: 'var(--ocp-text-muted)', fontSize: '12px' }}>
            Installée : <strong>{quantiteInstallee}</strong>
          </span>
        )}
      </div>

      <div style={{ position: 'relative', width: '100%', overflowX: 'auto' }}>
        <svg viewBox={`0 0 ${width} ${height}`} style={{ width: '100%', height: 'auto', display: 'block' }}>
          <defs>
            <linearGradient id="areaGradient" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor="#3b82f6" stopOpacity="0.3" />
              <stop offset="100%" stopColor="#3b82f6" stopOpacity="0.0" />
            </linearGradient>
          </defs>

          {yTicks.map((val, i) => {
            const y = getY(val);
            return (
              <g key={i}>
                <line x1={padding.left} y1={y} x2={width - padding.right} y2={y} stroke="var(--ocp-border-light)" strokeWidth="1" />
                <text x={padding.left - 10} y={y + 4} textAnchor="end" fontSize="11" fill="var(--ocp-text-muted)">
                  {val.toFixed(1)}
                </text>
              </g>
            );
          })}

          {seuilY !== null && (
            <g>
              <line
                x1={padding.left}
                y1={seuilY}
                x2={width - padding.right}
                y2={seuilY}
                stroke="#ef4444"
                strokeWidth="1.5"
                strokeDasharray="4 4"
              />
              <text x={width - padding.right} y={seuilY - 6} textAnchor="end" fontSize="11" fill="#ef4444" fontWeight="600">
                Seuil OCP
              </text>
            </g>
          )}

          {areaPath && <path d={areaPath} fill="url(#areaGradient)" />}

          {historyPath && (
            <path d={historyPath} fill="none" stroke="#3b82f6" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" />
          )}

          {predictionLinkPath && (
            <path d={predictionLinkPath} fill="none" stroke="#8b5cf6" strokeWidth="2" strokeDasharray="3 3" />
          )}

          {historyCoords.map(({ x, y, d }, i) => (
            <g key={i}>
              <circle
                cx={x}
                cy={y}
                r="5"
                fill="#ffffff"
                stroke="#3b82f6"
                strokeWidth="2.5"
                style={{ cursor: 'pointer', transition: 'all 0.2s' }}
                onMouseEnter={() => setHoveredPoint(d)}
                onMouseLeave={() => setHoveredPoint(null)}
              />
              <text x={x} y={padding.top + chartHeight + 18} textAnchor="middle" fontSize="11" fill="var(--ocp-text-muted)">
                {d.mois}
              </text>
            </g>
          ))}

          {predCoord && (
            <g>
              <circle
                cx={predCoord.x}
                cy={predCoord.y}
                r="6"
                fill="#8b5cf6"
                stroke="#ffffff"
                strokeWidth="2"
                style={{ cursor: 'pointer' }}
                onMouseEnter={() => setHoveredPoint(dataPoints[dataPoints.length - 1])}
                onMouseLeave={() => setHoveredPoint(null)}
              />
              <text
                x={predCoord.x}
                y={padding.top + chartHeight + 18}
                textAnchor="middle"
                fontSize="11"
                fill="#8b5cf6"
                fontWeight="700"
              >
                M+1
              </text>
            </g>
          )}
        </svg>

        {hoveredPoint && (
          <div
            style={{
              position: 'absolute',
              top: '12px',
              right: '16px',
              background: 'rgba(15, 23, 42, 0.9)',
              color: '#fff',
              padding: '6px 12px',
              borderRadius: '8px',
              fontSize: '12px',
              boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1)',
            }}
          >
            <strong>{hoveredPoint.mois}</strong>: {hoveredPoint.quantite.toFixed(2)} unités
            {hoveredPoint.isPrediction && ' (Prévision IA)'}
          </div>
        )}
      </div>
    </div>
  );
};

export default PdrTrendChart;
